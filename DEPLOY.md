# Tarokk – éles deploy (Hetzner VPS + Docker)

Ez az útmutató végigvezet a tarokk játék élesítésén egy Hetzner Cloud
szerveren. Nem kell hozzá korábbi szerveres tapasztalat – elég copy-paste-elni
a parancsokat. A végén a játék HTTPS-en, egy `https://...duckdns.org` címen
lesz elérhető a barátaidnak.

A stack három konténerből áll (Postgres + Spring Boot + nginx-frontend), elé
kerül egy **Caddy** reverse proxy, ami automatikusan intézi az ingyenes
HTTPS-tanúsítványt.

---

## 0. Mire lesz szükséged

- Egy bankkártya a Hetzner-regisztrációhoz (a gép kb. **€3,8/hó**, fix díj).
- ~30 perc.
- Semmi mást nem kell telepítened a saját gépedre.

---

## 1. Ingyenes domain a DuckDNS-en

A HTTPS-hez kell egy név. A DuckDNS ingyenes.

1. Menj a <https://www.duckdns.org> oldalra, lépj be (pl. GitHub-fiókkal).
2. Írj be egy szabad nevet (pl. `tarokk-bencsics`) és kattints **add domain**.
   Így a domained: `tarokk-bencsics.duckdns.org`.
3. Az IP-címet majd a 3. lépés után állítjuk be – hagyd nyitva a fület.

---

## 2. Hetzner szerver létrehozása

1. Regisztrálj: <https://accounts.hetzner.com/signUp> (a Hetzner **Cloud** kell).
2. A Cloud Console-ban: **New Project** → nevezd el (pl. `tarokk`).
3. **Add Server**:
   - **Location:** Nuremberg vagy Falkenstein (Németország, közel van).
   - **Image:** **Ubuntu 24.04**.
   - **Type:** a **CX22** (Shared vCPU, x86, 2 vCPU / 4 GB RAM) – ez kell, mert
     a Spring Boot fordítása RAM-igényes.
   - **Networking:** a Public IPv4 maradjon bekapcsolva.
   - **SSH key:** ha nem ismered, hagyd ki – akkor a szerver jelszavát
     e-mailben küldik. (Haladóknak: SSH-kulcs feltöltése kényelmesebb.)
   - **Name:** pl. `tarokk-prod`.
4. **Create & Buy now**. Pár másodperc, és kapsz egy **publikus IP-címet** –
   jegyezd fel (pl. `203.0.113.45`).

---

## 3. A domain rámutatása a szerverre

Vissza a DuckDNS-fülre:

1. A `current ip` mezőbe írd be a Hetzner IP-címet (a 2.4-es lépésből).
2. Kattints **update ip**.

Pár perc, és a `tarokk-bencsics.duckdns.org` a szerveredre fog mutatni.

---

## 4. Belépés a szerverre (SSH)

A saját gépeden nyiss egy terminált:

```bash
ssh root@203.0.113.45        # ← a saját IP-címedet írd be
```

- Ha jelszót kértek e-mailben, írd be (első belépéskor új jelszót kér).
- Ha „are you sure you want to continue connecting?” – írd be: `yes`.

Mostantól a parancsok **a szerveren** futnak (a `#` jel ezt jelzi).

---

## 5. Docker telepítése a szerverre

Másold be egyben:

```bash
apt update && apt upgrade -y
curl -fsSL https://get.docker.com | sh
```

Ellenőrzés:

```bash
docker --version && docker compose version
```

Mindkettő verziószámot ír ki → kész.

---

## 6. A projekt letöltése

```bash
cd /opt
git clone https://github.com/bencsicszoli/Hungarian-tarokk.git tarokk
cd tarokk
```

---

## 7. A titkok beállítása (.env)

```bash
cp .env.example .env
nano .env
```

Töltsd ki **éles, erős** értékekkel:

- `POSTGRES_PASSWORD` – találj ki egy hosszú, véletlen jelszót.
- `JWT_SECRET` – generálj egyet: nyiss egy **másik** terminált a szerveren,
  vagy futtasd: `openssl rand -base64 48`, és másold be az eredményt.
- `DOMAIN` – a duckdns neved, pl. `tarokk-bencsics.duckdns.org`.
- `ACME_EMAIL` – a saját e-mail-címed.

Mentés a nano-ban: `Ctrl+O`, `Enter`, majd kilépés: `Ctrl+X`.

---

## 8. Indítás 🚀

```bash
docker compose -f docker-compose.prod.yml up -d --build
```

Az **első** build több percig tart (Maven letölti a függőségeket). Türelem.

Nézd a logokat:

```bash
docker compose -f docker-compose.prod.yml logs -f
```

Amikor a Caddy logban megjelenik a `certificate obtained successfully`, és a
backend `Started ...Application`, kész. Kilépés a logból: `Ctrl+C` (a konténerek
tovább futnak).

Nyisd meg a böngészőben: **https://tarokk-bencsics.duckdns.org** 🎉

---

## Napi használat – hasznos parancsok

A szerveren, a `/opt/tarokk` mappában:

| Cél | Parancs |
|---|---|
| Állapot | `docker compose -f docker-compose.prod.yml ps` |
| Logok | `docker compose -f docker-compose.prod.yml logs -f` |
| Leállítás | `docker compose -f docker-compose.prod.yml down` |
| Újraindítás | `docker compose -f docker-compose.prod.yml restart` |

### Frissítés új kód után

```bash
cd /opt/tarokk
git pull
docker compose -f docker-compose.prod.yml up -d --build
```

### Adatbázis-mentés

```bash
docker exec tarokk-db pg_dump -U tarokk tarokk > backup-$(date +%F).sql
```

---

## Hibaelhárítás

- **A böngésző „nem biztonságos” / nincs HTTPS:** általában a DNS még nem állt
  be, vagy a 80/443 port nem elérhető. Várj pár percet, majd:
  `docker compose -f docker-compose.prod.yml logs caddy`.
- **A backend újraindulgat:** `docker compose -f docker-compose.prod.yml logs backend`
  – legtöbbször hibás adatbázis-jelszó az `.env`-ben.
- **„Cannot connect” a build során / kevés a RAM:** győződj meg róla, hogy a
  CX22 (4 GB) gépet választottad, ne a kisebbet.
