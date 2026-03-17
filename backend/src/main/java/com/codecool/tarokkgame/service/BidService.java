package com.codecool.tarokkgame.service;

import com.codecool.tarokkgame.constants.BidLevel;
import com.codecool.tarokkgame.constants.GameState;
import com.codecool.tarokkgame.constants.RoleInGame;
import com.codecool.tarokkgame.model.dto.SpecialBidCasesDTO;
import com.codecool.tarokkgame.model.dto.messagedto.PotentialBidsDTO;
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
            String level3 = BidLevel.THREE.getDescription();
            String level2 = BidLevel.TWO.getDescription();
            String level1 = BidLevel.ONE.getDescription();
            String pass = BidLevel.PASS.getDescription();
            Set<String> bids = new LinkedHashSet<>();
            SpecialBidCasesDTO specialBidCases = player.getSpecialBidCases();
            if (specialBidCases.isCouldInviteWith18() && specialBidCases.isCouldInviteWith19()) {
                return getPotentialBidsWithDifferentOptionsAtFirstBid(level3, level2, level1, pass, bids);

            } else if (specialBidCases.isCouldInviteWith18()) {
                return getPotentialBidsWithDifferentOptionsAtFirstBid(level3, null, level1, pass, bids);

            } else if (specialBidCases.isCouldInviteWith19()) {
                return getPotentialBidsWithDifferentOptionsAtFirstBid(level3, level2, null, pass, bids);

            } else {
                return getPotentialBidsWithDifferentOptionsAtFirstBid(level3, null, null, pass, bids);
            }
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
            game.setBiddingPasses(game.getBiddingPasses() + 1);
            sender.setBidLevel(bidLevel);
            playerRepository.save(sender);

            // Bidding PASS three times
            if (game.getBiddingPasses() == 3 && game.getBidLevel() != BidLevel.NONE) {
                game.setState(GameState.TALON_PICK_UP);
                game.setStartPlayer(game.getDeclarer());
                game.setTurnPlayer(game.getDeclarer());
                Player declarer = game.getPlayerByName(game.getDeclarer());
                declarer.setRoleInGame(RoleInGame.DECLARER);
                gameRepository.save(game);
                playerRepository.save(declarer);
                return null;

                // Bidding PASS less than three times
            } else {

                // Find the next bidding player
                Player nextPlayer = game.getNextBiddingPlayer(sender);
                if (nextPlayer != null) {
                    game.setTurnPlayer(nextPlayer.getName());

                    // Method in case of first bidder
                    if (nextPlayer.getBidLevel() == BidLevel.NONE) {

                        // There is no honour
                        if (!nextPlayer.hasAnyHonours()) {
                            gameRepository.save(game);
                            playerRepository.save(nextPlayer);
                            return new PotentialBidsDTO(Set.of(BidLevel.PASS.getDescription()), "game.potentialBids");
                        } else {

                            // Player has at least one honour
                            SpecialBidCasesDTO specialBidCases = nextPlayer.getSpecialBidCases();
                            BidLevel actualLevel = game.getBidLevel();
                            BidLevel levelUpByOneStep = actualLevel.getNextLevelAtFirstBid(actualLevel, 1);

                            // Player has at least 5 tarokks and the 18 and the 19
                            if (specialBidCases.isCouldInviteWith18() && specialBidCases.isCouldInviteWith19()) {
                                BidLevel levelUpByTwoSteps = actualLevel.getNextLevelAtFirstBid(actualLevel, 2);
                                BidLevel levelUpByThreeSteps = actualLevel.getNextLevelAtFirstBid(actualLevel, 3);
                                gameRepository.save(game);
                                playerRepository.save(nextPlayer);
                                bids.add(levelUpByOneStep.getDescription());
                                bids.add(levelUpByTwoSteps.getDescription());
                                bids.add(levelUpByThreeSteps.getDescription());
                                bids.add(BidLevel.PASS.getDescription());
                                return new PotentialBidsDTO(bids, "game.potentialBids");

                                // Player has at least 5 tarokks and the 19
                            } else if (specialBidCases.isCouldInviteWith19()) {
                                BidLevel levelUpByTwoSteps = actualLevel.getNextLevelAtFirstBid(actualLevel, 2);
                                //sender.setBidLevel(levelUpByOneStep);
                                gameRepository.save(game);
                                playerRepository.save(nextPlayer);
                                bids.add(levelUpByOneStep.getDescription());
                                bids.add(levelUpByTwoSteps.getDescription());
                                bids.add(BidLevel.PASS.getDescription());
                                return new PotentialBidsDTO(bids, "game.potentialBids");

                                // Player has at least 5 tarokks and the 18
                            } else if (specialBidCases.isCouldInviteWith18()) {
                                BidLevel levelUpByThreeSteps = actualLevel.getNextLevelAtFirstBid(actualLevel, 3);
                                //sender.setBidLevel(levelUpByOneStep);
                                gameRepository.save(game);
                                playerRepository.save(nextPlayer);
                                bids.add(levelUpByOneStep.getDescription());
                                bids.add(levelUpByThreeSteps.getDescription());
                                bids.add(BidLevel.PASS.getDescription());
                                return new PotentialBidsDTO(bids, "game.potentialBids");

                                // Player cannot invite
                            } else {
                                //sender.setBidLevel(levelUpByOneStep);
                                gameRepository.save(game);
                                playerRepository.save(nextPlayer);
                                bids.add(levelUpByOneStep.getDescription());
                                bids.add(BidLevel.PASS.getDescription());
                                return new PotentialBidsDTO(bids, "game.potentialBids");
                            }
                        }

                        // Method in case of bidder has already bid
                    } else {
                        SpecialBidCasesDTO specialBidCases = nextPlayer.getSpecialBidCases();
                        BidLevel actualLevel = game.getBidLevel();
                        BidLevel levelUpByOneStep = actualLevel.getNextLevelAtOtherBids(actualLevel, 1);
                        BidLevel levelUpByTwoSteps = actualLevel.getNextLevelAtOtherBids(actualLevel, 2);
                        BidLevel levelUpByThreeSteps = actualLevel.getNextLevelAtOtherBids(actualLevel, 3);
                        BidLevel levelUpByFourSteps = actualLevel.getNextLevelAtOtherBids(actualLevel, 4);

                        // Player has the 20 and at least 5 tarokks, and bidLevel is two
                        if (actualLevel == BidLevel.TWO && specialBidCases.isCouldYieldWith20()) {

                            // Player has the 18, 19, 20 and at least 5 tarokks, and bidLevel is two
                            if (specialBidCases.isCouldInviteWith18() && specialBidCases.isCouldInviteWith19()) {
                                //sender.setBidLevel(levelUpByOneStep);
                                gameRepository.save(game);
                                playerRepository.save(nextPlayer);
                                bids.add(levelUpByOneStep.getDescription());
                                bids.add(levelUpByTwoSteps.getDescription());
                                //bids.add(levelUpByThreeSteps.getDescription());
                                bids.add(levelUpByFourSteps.getDescription());
                                bids.add(BidLevel.PASS.getDescription());
                                return new PotentialBidsDTO(bids, "game.potentialBids");

                                // Player has the 19, 20 and at least 5 tarokks, and bidLevel is two
                            } else if (specialBidCases.isCouldInviteWith19()) {
                                //sender.setBidLevel(levelUpByOneStep);
                                gameRepository.save(game);
                                playerRepository.save(nextPlayer);
                                bids.add(levelUpByOneStep.getDescription());
                                bids.add(levelUpByTwoSteps.getDescription());
                                bids.add(BidLevel.PASS.getDescription());
                                return new PotentialBidsDTO(bids, "game.potentialBids");

                                // Player has the 18, 20 and at least 5 tarokks, and bidLevel is two
                            } else if (specialBidCases.isCouldInviteWith18()) {
                                //sender.setBidLevel(levelUpByOneStep);
                                bids.add(levelUpByOneStep.getDescription());
                                //bids.add(levelUpByThreeSteps.getDescription());
                                bids.add(levelUpByFourSteps.getDescription());
                                bids.add(BidLevel.PASS.getDescription());
                                return new PotentialBidsDTO(bids, "game.potentialBids");

                                // Player has the 20 and at least 5 tarokks, and bidLevel is two
                            } else {
                                //sender.setBidLevel(levelUpByOneStep);
                                bids.add(levelUpByOneStep.getDescription());
                                bids.add(BidLevel.PASS.getDescription());
                                return new PotentialBidsDTO(bids, "game.potentialBids");
                            }

                        } else {
                            // Player cannot announce 'PASS'
                            if (actualLevel == BidLevel.TWO) {
                                if (specialBidCases.isCouldInviteWith18() && specialBidCases.isCouldInviteWith19()) {
                                    gameRepository.save(game);
                                    playerRepository.save(nextPlayer);
                                    bids.add(levelUpByOneStep.getDescription());
                                    bids.add(levelUpByTwoSteps.getDescription());
                                    bids.add(levelUpByFourSteps.getDescription());  // ?????????
                                    bids.add(BidLevel.PASS.getDescription());
                                    return new PotentialBidsDTO(bids, "game.potentialBids");
                                } else if (specialBidCases.isCouldInviteWith19()) {
                                    gameRepository.save(game);
                                    playerRepository.save(nextPlayer);
                                    bids.add(levelUpByOneStep.getDescription());
                                    bids.add(levelUpByTwoSteps.getDescription());
                                    bids.add(BidLevel.PASS.getDescription());
                                    return new PotentialBidsDTO(bids, "game.potentialBids");
                                } else if (specialBidCases.isCouldInviteWith18()) {
                                    gameRepository.save(game);
                                    playerRepository.save(nextPlayer);
                                    bids.add(levelUpByOneStep.getDescription());
                                    bids.add(levelUpByFourSteps.getDescription());  // ????????????
                                    bids.add(BidLevel.PASS.getDescription());
                                    return new PotentialBidsDTO(bids, "game.potentialBids");
                                } else {
                                    gameRepository.save(game);
                                    playerRepository.save(nextPlayer);
                                    return new PotentialBidsDTO(
                                            Set.of(levelUpByOneStep.getDescription()),
                                            "game.potentialBids");
                                }
                                //sender.setBidLevel(levelUpByOneStep);


                                // Player can announce 'PASS'
                            } else {
                                //sender.setBidLevel(levelUpByOneStep);
                                gameRepository.save(game);
                                playerRepository.save(nextPlayer);
                                if (specialBidCases.isCouldInviteWith19()) {
                                    gameRepository.save(game);
                                    playerRepository.save(nextPlayer);
                                    bids.add(levelUpByOneStep.getDescription());
                                    bids.add(levelUpByTwoSteps.getDescription());
                                    bids.add(BidLevel.PASS.getDescription());
                                    return new PotentialBidsDTO(bids, "game.potentialBids");
                                } else {
                                    //sender.setBidLevel(levelUpByOneStep);
                                    gameRepository.save(game);
                                    playerRepository.save(nextPlayer);
                                    bids.add(levelUpByOneStep.getDescription());
                                    bids.add(BidLevel.PASS.getDescription());
                                    return new PotentialBidsDTO(bids, "game.potentialBids");
                                }
                            }
                        }
                    }

                    // We cannot find the next bidding player
                } else {
                    throw new NoSuchElementException("Next player not found in bidding phase");
                }
            }

            // Player's bid is not PASS
        } else {
            // There has been a XIX invit, but the announcer is not the sender
            game.setDeclarer(sender.getName());
            System.out.println("Sender: " + sender.getName());
            //game.setBidLevel(bidLevel);  Set it later!
            game.setInformation(bidLevel.getDescription());

            if (game.isXIXInvit() && !sender.isAnnouncedXIX_Invit()) {
                sender.setAcceptedXIX_Invit(true);
                game.setInvitAcceptor(sender.getName());

                // There has been a XVIII invit, but the announcer is not the sender
            } else if (game.isXVIIIInvit() && !sender.isAnnouncedXVIII_Invit()) {
                sender.setAcceptedXVIII_Invit(true);
                game.setInvitAcceptor(sender.getName());
            }
            // The sender has bid first time
            if (sender.getBidLevel() == BidLevel.NONE) { // Incorrect!
                if (bidLevel.getBidValue() - game.getBidLevel().getBidValue() == 2) {
                    game.setXIXInvit(true);
                    sender.setAnnouncedXIX_Invit(true);
                    sender.setBidLevel(bidLevel);
                } else if (bidLevel.getBidValue() - game.getBidLevel().getBidValue() == 3) {
                    game.setXVIIIInvit(true);
                    sender.setAnnouncedXVIII_Invit(true);
                    sender.setBidLevel(bidLevel);
                } else {
                    sender.setBidLevel(bidLevel);
                }

                // The sender has already bid
            } else {
                if (bidLevel.getGrade() - game.getBidLevel().getGrade() == 2) {
                    game.setXIXInvit(true);
                    sender.setAnnouncedXIX_Invit(true);
                    sender.setBidLevel(bidLevel);
                } else if (bidLevel.getGrade() - game.getBidLevel().getGrade() == 3 ||
                        bidLevel.getGrade() - sender.getBidLevel().getGrade() == 4) {
                    game.setXVIIIInvit(true);
                    sender.setAnnouncedXVIII_Invit(true);
                    sender.setBidLevel(bidLevel);
                } else {
                    sender.setBidLevel(bidLevel);
                }
            }
            game.setBidLevel(bidLevel);

            // There is no more option to continue bidding
            if (bidLevel == BidLevel.SOLO_HELD || game.getBiddingPasses() == 3) {
                game.setState(GameState.TALON_PICK_UP);
                game.setStartPlayer(game.getDeclarer());
                game.setTurnPlayer(game.getDeclarer());
                Player declarer = game.getPlayerByName(game.getDeclarer());
                declarer.setRoleInGame(RoleInGame.DECLARER);
                gameRepository.save(game);
                playerRepository.save(declarer);
                playerRepository.save(sender);
                return null;

                // There are opportunities to continue bidding
            } else {
                playerRepository.save(sender);
                Player nextPlayer = game.getNextBiddingPlayer(sender);
                if (nextPlayer != null) {
                    game.setTurnPlayer(nextPlayer.getName());

                    // Method in case of first bidder
                    if (nextPlayer.getBidLevel() == BidLevel.NONE) {

                        // There is no honour
                        if (!nextPlayer.hasAnyHonours()) {
                            //sender.setBidLevel(bidLevel);
                            gameRepository.save(game);
                            //playerRepository.save(sender);
                            playerRepository.save(nextPlayer);
                            return new PotentialBidsDTO(Set.of(BidLevel.PASS.getDescription()), "game.potentialBids");
                        } else {

                            // Player has at least one honour
                            SpecialBidCasesDTO specialBidCases = nextPlayer.getSpecialBidCases();
                            BidLevel actualLevel = game.getBidLevel();
                            BidLevel levelUpByOneStep = actualLevel.getNextLevelAtFirstBid(actualLevel, 1);

                            // Player has at least 5 tarokks and the 18 and the 19
                            if (specialBidCases.isCouldInviteWith18() && specialBidCases.isCouldInviteWith19()) {
                                BidLevel levelUpByTwoSteps = actualLevel.getNextLevelAtFirstBid(actualLevel, 2);
                                BidLevel levelUpByThreeSteps = actualLevel.getNextLevelAtFirstBid(actualLevel, 3);
                                //sender.setBidLevel(levelUpByOneStep);
                                //playerRepository.save(sender);
                                gameRepository.save(game);
                                playerRepository.save(nextPlayer);
                                bids.add(levelUpByOneStep.getDescription());
                                bids.add(levelUpByTwoSteps.getDescription());
                                bids.add(levelUpByThreeSteps.getDescription());
                                bids.add(BidLevel.PASS.getDescription());
                                return new PotentialBidsDTO(bids, "game.potentialBids");

                                // Player has at least 5 tarokks and the 19
                            } else if (specialBidCases.isCouldInviteWith19()) {
                                BidLevel levelUpByTwoSteps = actualLevel.getNextLevelAtFirstBid(actualLevel, 2);
                                //sender.setBidLevel(levelUpByOneStep);
                                //playerRepository.save(sender);
                                gameRepository.save(game);
                                playerRepository.save(nextPlayer);
                                bids.add(levelUpByOneStep.getDescription());
                                bids.add(levelUpByTwoSteps.getDescription());
                                bids.add(BidLevel.PASS.getDescription());
                                return new PotentialBidsDTO(bids, "game.potentialBids");

                                // Player has at least 5 tarokks and the 18
                            } else if (specialBidCases.isCouldInviteWith18()) {
                                BidLevel levelUpByThreeSteps = actualLevel.getNextLevelAtFirstBid(actualLevel, 3);
                                //sender.setBidLevel(levelUpByOneStep);
                                //playerRepository.save(sender);
                                gameRepository.save(game);
                                playerRepository.save(nextPlayer);
                                bids.add(levelUpByOneStep.getDescription());
                                bids.add(levelUpByThreeSteps.getDescription());
                                bids.add(BidLevel.PASS.getDescription());
                                return new PotentialBidsDTO(bids, "game.potentialBids");

                                // Player cannot invite
                            } else {
                                //sender.setBidLevel(levelUpByOneStep);
                                //playerRepository.save(sender);
                                gameRepository.save(game);
                                playerRepository.save(nextPlayer);
                                bids.add(levelUpByOneStep.getDescription());
                                bids.add(BidLevel.PASS.getDescription());
                                return new PotentialBidsDTO(bids, "game.potentialBids");
                            }
                        }

                        // Method in case of bidder has already bid
                    } else {
                        SpecialBidCasesDTO specialBidCases = nextPlayer.getSpecialBidCases();
                        BidLevel actualLevel = game.getBidLevel();
                        BidLevel levelUpByOneStep = actualLevel.getNextLevelAtOtherBids(actualLevel, 1);
                        BidLevel levelUpByTwoSteps = actualLevel.getNextLevelAtOtherBids(actualLevel, 2);
                        BidLevel levelUpByThreeSteps = actualLevel.getNextLevelAtOtherBids(actualLevel, 3);
                        BidLevel levelUpByFourSteps = actualLevel.getNextLevelAtOtherBids(actualLevel, 4);

                        // Player has the 20 and at least 5 tarokks, and bidLevel is two
                        if (actualLevel == BidLevel.TWO && specialBidCases.isCouldYieldWith20()) {

                            // Player has the 18, 19, 20 and at least 5 tarokks, and bidLevel is two
                            if (specialBidCases.isCouldInviteWith18() && specialBidCases.isCouldInviteWith19()) {
                                //sender.setBidLevel(levelUpByOneStep);
                                //playerRepository.save(sender);
                                gameRepository.save(game);
                                playerRepository.save(nextPlayer);
                                bids.add(levelUpByOneStep.getDescription());
                                bids.add(levelUpByTwoSteps.getDescription());
                                //bids.add(levelUpByThreeSteps.getDescription());
                                bids.add(levelUpByFourSteps.getDescription());
                                bids.add(BidLevel.PASS.getDescription());
                                return new PotentialBidsDTO(bids, "game.potentialBids");

                                // Player has the 19, 20 and at least 5 tarokks, and bidLevel is two
                            } else if (specialBidCases.isCouldInviteWith19()) {
                                //sender.setBidLevel(levelUpByOneStep);
                                //playerRepository.save(sender);
                                gameRepository.save(game);
                                playerRepository.save(nextPlayer);
                                bids.add(levelUpByOneStep.getDescription());
                                bids.add(levelUpByTwoSteps.getDescription());
                                bids.add(BidLevel.PASS.getDescription());
                                return new PotentialBidsDTO(bids, "game.potentialBids");

                                // Player has the 18, 20 and at least 5 tarokks, and bidLevel is two
                            } else if (specialBidCases.isCouldInviteWith18()) {
                                //sender.setBidLevel(levelUpByOneStep);
                                //playerRepository.save(sender);
                                bids.add(levelUpByOneStep.getDescription());
                                bids.add(levelUpByFourSteps.getDescription());
                                bids.add(BidLevel.PASS.getDescription());
                                return new PotentialBidsDTO(bids, "game.potentialBids");

                                // Player has the 20 and at least 5 tarokks, and bidLevel is two
                            } else {
                                //sender.setBidLevel(levelUpByOneStep);
                                //playerRepository.save(sender);
                                bids.add(levelUpByOneStep.getDescription());
                                bids.add(BidLevel.PASS.getDescription());
                                return new PotentialBidsDTO(bids, "game.potentialBids");
                            }

                        } else {
                            // Player cannot announce 'PASS'
                            if (actualLevel == BidLevel.TWO) {
                                //sender.setBidLevel(levelUpByOneStep);
                                //playerRepository.save(sender);
                                gameRepository.save(game);
                                playerRepository.save(nextPlayer);
                                return new PotentialBidsDTO(
                                        Set.of(levelUpByOneStep.getDescription()),
                                        "game.potentialBids");
                                // Player can announce 'PASS'
                            } else {
                                //sender.setBidLevel(levelUpByOneStep);
                                //playerRepository.save(sender);
                                gameRepository.save(game);
                                playerRepository.save(nextPlayer);
                                if (specialBidCases.isCouldInviteWith19()) {
                                    gameRepository.save(game);
                                    playerRepository.save(nextPlayer);
                                    bids.add(levelUpByOneStep.getDescription());
                                    bids.add(levelUpByTwoSteps.getDescription());
                                    bids.add(BidLevel.PASS.getDescription());
                                    return new PotentialBidsDTO(bids, "game.potentialBids");
                                } else {
                                    //sender.setBidLevel(levelUpByOneStep);
                                    //playerRepository.save(sender);
                                    gameRepository.save(game);
                                    playerRepository.save(nextPlayer);
                                    bids.add(levelUpByOneStep.getDescription());
                                    bids.add(BidLevel.PASS.getDescription());
                                    return new PotentialBidsDTO(bids, "game.potentialBids");
                                }
                            }
                        }
                    }

                    // We cannot find the next bidding player
                } else {
                    throw new NoSuchElementException("Next player not found in bidding phase");
                }
            }
        }
    }

    private PotentialBidsDTO getPotentialBidsWithDifferentOptionsAtFirstBid(String level3, String level2, String level1, String pass, Set<String> options) {
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

    private PotentialBidsDTO getPotentialBidsWithDifferentOptionsAtOtherBids(String option1, String option2, String option3, String option4, Set<String> options, Game game, Player player) {
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
}
