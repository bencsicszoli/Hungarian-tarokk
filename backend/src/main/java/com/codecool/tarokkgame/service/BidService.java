package com.codecool.tarokkgame.service;

import com.codecool.tarokkgame.constants.BidLevel;
import com.codecool.tarokkgame.constants.GameState;
import com.codecool.tarokkgame.constants.RoleInGame;
import com.codecool.tarokkgame.model.dto.SpecialBidCasesDTO;
import com.codecool.tarokkgame.model.dto.messagedto.response.PotentialBidsDTO;
import com.codecool.tarokkgame.model.dto.messagedto.response.PublicBidDTO;
import com.codecool.tarokkgame.model.entity.Game;
import com.codecool.tarokkgame.model.entity.Player;
import com.codecool.tarokkgame.repository.GameRepository;
import com.codecool.tarokkgame.repository.PlayerRepository;
import org.springframework.stereotype.Service;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Set;

@Service
public class BidService {
    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;

    public BidService(GameRepository gameRepository, PlayerRepository playerRepository) {
        this.gameRepository = gameRepository;
        this.playerRepository = playerRepository;
    }

    public PotentialBidsDTO getPotentialBidsToStartPlayer(String username, long gameId) {
        Player player = playerRepository.findByUserUsernameAndGameId(username, gameId).orElseThrow(() -> new NoSuchElementException("Player not found"));
        boolean hasAnyHonours = player.hasAnyHonours();
        if (!hasAnyHonours) {
            return new PotentialBidsDTO(Set.of(BidLevel.PASS.getDescription()), "game.firstPotentialBids");
        } else {
            return createBidCasesToStartPlayer(player);
        }
    }

    public PotentialBidsDTO getPotentialBidsToTurnPlayer(String username, long gameId, String newBidLevel) {
        Player sender = playerRepository.findByUserUsernameAndGameId(username, gameId).orElseThrow(() -> new NoSuchElementException("Player not found"));
        BidLevel bidLevel = BidLevel.getLevelByDescription(newBidLevel);
        System.out.println(bidLevel);
        Game game = gameRepository.findById(gameId).orElseThrow(() -> new NoSuchElementException("Game not found"));
        Set<String> bids = new LinkedHashSet<>();

        // Sender's bid is PASS
        if (bidLevel == BidLevel.PASS) {
            return handleBidIfPass(game, sender, bidLevel, bids);

        // Player's bid is not PASS
        } else {
            return handleBidIfNotPass(game, sender, bidLevel, bids);
        }
    }

    public PublicBidDTO getPublicBidInfo(Game game, String turnPlayer) {
        String declarer = game.getDeclarer();
        String bid = game.getBidLevel().getBidNameToDisplay();
        System.out.println("Bid name: " + bid);
        return new PublicBidDTO(declarer, turnPlayer, bid, "game.publicBidInfo");
    }

    private PotentialBidsDTO getPotentialBidsWithDifferentOptionsAtFirstTime(String level3, String level2, String level1, String pass, Set<String> options) {
        options.add(level3);
        if (level2 != null) {
            options.add(level2);
        }
        if (level1 != null) {
            options.add(level1);
        }
        options.add(pass);

        return new PotentialBidsDTO(options, "game.firstPotentialBids");
    }

    private PotentialBidsDTO getPotentialBidsWithDifferentOptionsAtFurtherCases(String option1, String option2, String option3, String option4, Set<String> options, Game game, Player player) {
        gameRepository.save(game);
        playerRepository.save(player);
        options.add(option1);
        if (option2 != null) {
            options.add(option2);
        }
        if (option3 != null) {
            options.add(option3);
        }
        if (option4 != null) {
            options.add(option4);
        }
        return new PotentialBidsDTO(options, "game.potentialBids");
    }

    private PotentialBidsDTO handleBidIfPass(Game game, Player sender, BidLevel bidLevel, Set<String> bids) {
        game.setBiddingPasses(game.getBiddingPasses() + 1);

        // Handle yielded game
        if (sender.getBidLevel() == BidLevel.THREE && game.getBidLevel() == BidLevel.TWO) {
            sender.setYieldedGame(true);
            game.setYielded(true);
        }
        sender.setBidLevel(bidLevel);

        // Bidding PASS three times
        if (game.getBiddingPasses() == 3 && game.getBidLevel() != BidLevel.NONE) {
            return finishBidding(game, sender);

        // Bidding PASS less than three times
        } else {
            return createBidCases(sender, game, bids);
        }
    }

    private PotentialBidsDTO handleBidIfNotPass(Game game, Player sender, BidLevel bidLevel, Set<String> bids) {
        game.setDeclarer(sender.getName());
        game.setInformation(bidLevel.getDescription());
        //sender.setBidLevel(bidLevel); // később !!!

        handleInvitAcceptance(game, sender);

        handleInvitAnnouncement(sender, bidLevel, game);

        game.setBidLevel(bidLevel);
        sender.setBidLevel(bidLevel);

        // There is no more option to continue bidding
        if (bidLevel == BidLevel.SOLO_HELD || game.getBiddingPasses() == 3) {
            return finishBidding(game, sender);

        // There is at least one further option to continue bidding
        } else {
            return createBidCases(sender, game, bids);
        }
    }

    private PotentialBidsDTO methodInCaseFirstBidder(Player nextPlayer, Game game, Set<String> bids) {

        // Player has no honour
        if (!nextPlayer.hasAnyHonours()) {
            gameRepository.save(game);
            playerRepository.save(nextPlayer);
            return new PotentialBidsDTO(Set.of(BidLevel.PASS.getDescription()), "game.potentialBids");

        // Player has at least one honour
        } else {
            return createPotentialBidsToTurnPlayerAtFirstTime(nextPlayer, game, bids);
        }
    }

    private PotentialBidsDTO finishBidding(Game game, Player sender) {
        game.setState(GameState.TALON_PICK_UP);
        game.setTurnPlayer(game.getDeclarer());
        Player declarer = game.getPlayerByName(game.getDeclarer());
        declarer.setRoleInGame(RoleInGame.DECLARER);
        game.setPlayerRolesInCaseYieldedGameOrInvit(declarer);
        gameRepository.save(game);
        playerRepository.save(declarer);
        playerRepository.save(sender);
        return null;
    }

    private PotentialBidsDTO createBidCases(Player sender, Game game, Set<String> bids) {
        // Find the next bidding player
        playerRepository.save(sender);
        Player nextPlayer = game.getNextBiddingPlayer(sender);
        if (nextPlayer != null) {
            game.setTurnPlayer(nextPlayer.getName());

            // Method in case of first bidder
            if (nextPlayer.getBidLevel() == BidLevel.NONE) {

                return methodInCaseFirstBidder(nextPlayer, game, bids);

            // Method in case of bidder has already bid
            } else {
                SpecialBidCasesDTO specialBidCases = nextPlayer.getSpecialBidCases();
                BidLevel actualLevel = game.getBidLevel();
                String option1 = actualLevel.getNextLevelAtOtherBids(actualLevel, 1).getDescription();
                String option2;
                if (!actualLevel.getDescription().contains("Hold")) {
                    option2 = actualLevel.getNextLevelAtOtherBids(actualLevel, 2).getDescription();
                } else {
                    option2 = actualLevel.getNextLevelAtOtherBids(actualLevel, 3).getDescription();
                }
                String option3 = actualLevel.getNextLevelAtOtherBids(actualLevel, 4).getDescription();
                String option4 = BidLevel.PASS.getDescription();

                if (actualLevel == BidLevel.TWO && specialBidCases.isCouldYieldWith20()) {

                    // Player has the 20 and at least 5 tarokks, and bidLevel is two
                    return createBidCasesInCasePotentialYieldedGame(specialBidCases, nextPlayer, game, bids, option1, option2, option3, option4);

                } else {
                    // Player cannot announce 'PASS'
                    return createBidCasesWithoutPotentialYieldedGame(specialBidCases, nextPlayer, actualLevel, game, bids, option1, option2, option3, option4);
                }
            }

            // We cannot find the next bidding player
        } else {
            throw new NoSuchElementException("Next player not found in bidding phase");
        }
    }

    private PotentialBidsDTO createBidCasesToStartPlayer(Player player) {
        String level3 = BidLevel.THREE.getDescription();
        String level2 = BidLevel.TWO.getDescription();
        String level1 = BidLevel.ONE.getDescription();
        String pass = BidLevel.PASS.getDescription();
        Set<String> bids = new LinkedHashSet<>();
        SpecialBidCasesDTO specialBidCases = player.getSpecialBidCases();
        if (specialBidCases.isCouldInviteWith18() && specialBidCases.isCouldInviteWith19()) {
            return getPotentialBidsWithDifferentOptionsAtFirstTime(level3, level2, level1, pass, bids);

        } else if (specialBidCases.isCouldInviteWith18()) {
            return getPotentialBidsWithDifferentOptionsAtFirstTime(level3, null, level1, pass, bids);

        } else if (specialBidCases.isCouldInviteWith19()) {
            return getPotentialBidsWithDifferentOptionsAtFirstTime(level3, level2, null, pass, bids);

        } else {
            return getPotentialBidsWithDifferentOptionsAtFirstTime(level3, null, null, pass, bids);
        }
    }

    private PotentialBidsDTO createPotentialBidsToTurnPlayerAtFirstTime(Player nextPlayer, Game game, Set<String> bids) {
        SpecialBidCasesDTO specialBidCases = nextPlayer.getSpecialBidCases();
        BidLevel actualLevel = game.getBidLevel();
        String option1 = actualLevel.getNextLevelAtFirstBid(actualLevel, 1).getDescription();
        String option2 = actualLevel.getNextLevelAtFirstBid(actualLevel, 2).getDescription();
        String option3 = actualLevel.getNextLevelAtFirstBid(actualLevel, 3).getDescription();
        String option4 = BidLevel.PASS.getDescription();

        // Player has at least 5 tarokks and the 18 and the 19
        if (specialBidCases.isCouldInviteWith18() && specialBidCases.isCouldInviteWith19()) {
            return getPotentialBidsWithDifferentOptionsAtFurtherCases(option1, option2, option3, option4, bids, game,nextPlayer);

            // Player has at least 5 tarokks and the 19
        } else if (specialBidCases.isCouldInviteWith19()) {
            return getPotentialBidsWithDifferentOptionsAtFurtherCases(option1, option2, null, option4, bids, game, nextPlayer);

            // Player has at least 5 tarokks and the 18
        } else if (specialBidCases.isCouldInviteWith18()) {
            return getPotentialBidsWithDifferentOptionsAtFurtherCases(option1, null, option3, option4, bids, game, nextPlayer);

            // Player cannot invite
        } else {
            return getPotentialBidsWithDifferentOptionsAtFurtherCases(option1, null, null, option4, bids, game, nextPlayer);
        }
    }

    private void handleInvitAcceptance(Game game, Player sender) {

        // There has been a XIX invit, but the announcer is not the sender
        if (game.isXIXInvit() && !sender.isAnnouncedXIX_Invit()) {
            sender.setAcceptedXIX_Invit(true);
            game.setInvitAcceptor(sender.getName());

            // There has been a XVIII invit, but the announcer is not the sender
        } else if (game.isXVIIIInvit() && !sender.isAnnouncedXVIII_Invit()) {
            sender.setAcceptedXVIII_Invit(true);
            game.setInvitAcceptor(sender.getName());
        }
    }

    private void handleInvitAnnouncement(Player sender, BidLevel bidLevel, Game game) {

        // The sender has bid first time
        if (sender.getBidLevel() == BidLevel.NONE) {
            if (bidLevel.getBidValue() - game.getBidLevel().getBidValue() == 2) {
                game.setXIXInvit(true);
                sender.setAnnouncedXIX_Invit(true);
            } else if (bidLevel.getBidValue() - game.getBidLevel().getBidValue() == 3) {
                game.setXVIIIInvit(true);
                sender.setAnnouncedXVIII_Invit(true);
            }

            // The sender has already bid
        } else {
            if (bidLevel.getGrade() - game.getBidLevel().getGrade() == 2) {
                game.setXIXInvit(true);
                sender.setAnnouncedXIX_Invit(true);
            } else if (bidLevel.getGrade() - sender.getBidLevel().getGrade() == 4) {
                game.setXVIIIInvit(true);
                sender.setAnnouncedXVIII_Invit(true);
            }
        }
    }

    private PotentialBidsDTO createBidCasesInCasePotentialYieldedGame(SpecialBidCasesDTO specialBidCases, Player nextPlayer, Game game, Set<String> bids, String option1, String option2, String option3, String option4) {

        // Player has the 18, 19, 20 and at least 5 tarokks, and bidLevel is two
        if (specialBidCases.isCouldInviteWith18() && specialBidCases.isCouldInviteWith19()) {
            if (nextPlayer.isAnnouncedXIX_Invit()) {
                option2 = null;
            }
            return getPotentialBidsWithDifferentOptionsAtFurtherCases(option1, option2, option3, option4, bids, game, nextPlayer);

            // Player has the 19, 20 and at least 5 tarokks, and bidLevel is two
        } else if (specialBidCases.isCouldInviteWith19()) {
            if (nextPlayer.isAnnouncedXIX_Invit()) {
                option2 = null;
            }
            return getPotentialBidsWithDifferentOptionsAtFurtherCases(option1, option2, null, option4, bids, game, nextPlayer);

            // Player has the 18, 20 and at least 5 tarokks, and bidLevel is two
        } else if (specialBidCases.isCouldInviteWith18()) {
            return getPotentialBidsWithDifferentOptionsAtFurtherCases(option1, null, option3, option4, bids, game, nextPlayer);

            // Player has the 20 and at least 5 tarokks, and bidLevel is two
        } else {
            return getPotentialBidsWithDifferentOptionsAtFurtherCases(option1, null, null, option4, bids, game, nextPlayer);
        }
    }

    private PotentialBidsDTO createBidCasesWithoutPotentialYieldedGame(SpecialBidCasesDTO specialBidCases, Player nextPlayer, BidLevel actualLevel, Game game, Set<String> bids, String option1, String option2, String option3, String option4) {
        if (actualLevel == BidLevel.TWO) {

            // Player cannot announce 'PASS'
            if (specialBidCases.isCouldInviteWith18() && specialBidCases.isCouldInviteWith19()) {
                if (nextPlayer.isAnnouncedXIX_Invit()) {
                    option2 = null;
                }
                return getPotentialBidsWithDifferentOptionsAtFurtherCases(option1, option2, option3, null, bids, game, nextPlayer);
            } else if (specialBidCases.isCouldInviteWith19()) {
                if (nextPlayer.isAnnouncedXIX_Invit()) {
                    option2 = null;
                }
                return getPotentialBidsWithDifferentOptionsAtFurtherCases(option1, option2, null, null, bids, game, nextPlayer);
            } else if (specialBidCases.isCouldInviteWith18()) {
                return getPotentialBidsWithDifferentOptionsAtFurtherCases(option1, null, option3, null, bids, game, nextPlayer);
            } else {
                return getPotentialBidsWithDifferentOptionsAtFurtherCases(option1, null, null, null, bids, game, nextPlayer);
            }

        } else {
            // Player can announce 'PASS'
            if (specialBidCases.isCouldInviteWith19()) {
                if (nextPlayer.isAnnouncedXIX_Invit()) {
                    option2 = null;
                }
                return getPotentialBidsWithDifferentOptionsAtFurtherCases(option1, option2, null, option4, bids, game, nextPlayer);
            } else {
                return getPotentialBidsWithDifferentOptionsAtFurtherCases(option1, null, null, option4, bids, game, nextPlayer);
            }
        }
    }
}
