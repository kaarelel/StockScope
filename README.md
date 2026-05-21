# StockScope

Investeerimisanalüütika dashboard demo. Spring Boot REST API + Vue 3 frontend, mock-andmetel.

## Struktuur

```
StockScope/
├── pom.xml                  # Spring Boot backend (Maven)
├── src/main/java/...        # Backend kood
├── src/main/resources/
├── frontend/                # Vue 3 + TypeScript + Vite frontend
│   ├── package.json
│   ├── src/
│   └── ...
└── README.md
```

## Funktsionaalsus

- **Dashboard** — turu üldseis, indeksid, top tõusjad/langejad, aktiivsed + watchlist widget
- **Aktsiad** — täielik tabel otsingu, sortimise ja sektorifiltriga (~40 aktsiat 8 sektoris)
- **Stock detail** — hind, muutused, P/E, dividend, ülevaade ja automaatne kommentaar; nupp "Create alert from here"
- **Watchlist** — kasutaja saab luua nimekirjasid ja lisada aktsiaid; salvestub PostgreSQL-i, scope'itud cookie-põhise sessiooni järgi
- **Alerts & notifications** — kasutaja saab seada hinna-läve teavitusi (ABOVE / BELOW). `@Scheduled` job (30s) drift'ib mock-hindu ja vallandab täidetud tingimusega alertid. NavBar bell näitab lugemata teavituste arvu + viimase 10 dropdown'is.
- **Portfell** — kujuteldav investeering, sektorite jaotus, tootlus, üldine risk
- **Turuülevaated** — demo-trendid ja sektoripõhised tähelepanekud

## Backend (Spring Boot)

**Eeldused:** Java 17+, Maven (või kasuta `mvnw`), Docker (PostgreSQL).

### Lokaalne käivitamine

```powershell
# 1. PostgreSQL üles (esimene kord laeb image'i)
docker compose up -d

# 2. Spring Boot - Liquibase migrations jooksevad startupis
./mvnw spring-boot:run
```

Käivitub aadressil `http://localhost:8080`. Vaikimisi DB:
- URL: `jdbc:postgresql://localhost:5432/stockscope`
- User: `stockscope`, password: `stockscope` (ainult dev)

Tootmiseks kasuta env-muutujaid: `DB_URL`, `DB_USER`, `DB_PASSWORD`.

### REST endpoints

| Meetod | URL | Kirjeldus |
|--------|-----|-----------|
| GET | `/api/market/overview` | Turu kokkuvõte, indeksid, top liikujad |
| GET | `/api/stocks` | Aktsiate nimekiri (`?search=`, `?sector=`, `?sort=`, `?direction=`) |
| GET | `/api/stocks/sectors` | Saadaolevad sektorid |
| GET | `/api/stocks/gainers?limit=5` | Top tõusjad |
| GET | `/api/stocks/losers?limit=5` | Top langejad |
| GET | `/api/stocks/active?limit=5` | Aktiivseimad |
| GET | `/api/stocks/{symbol}` | Konkreetne aktsia |
| POST | `/api/portfolio/simulate` | Portfelli simulatsioon |
| GET | `/api/insights` | Turuülevaated |
| GET | `/api/watchlist` | Kasutaja sessiooni watchlistid |
| GET | `/api/watchlist/{id}` | Üksik watchlist |
| POST | `/api/watchlist` | Loo uus watchlist (body: `{"name":"..."}`) |
| PUT | `/api/watchlist/{id}` | Nimeta watchlist ümber |
| DELETE | `/api/watchlist/{id}` | Kustuta watchlist |
| PUT | `/api/watchlist/{id}/items` | Lisa sümbol (idempotent, case-insensitive) |
| DELETE | `/api/watchlist/{id}/items/{symbol}` | Eemalda sümbol |
| GET | `/api/watchlist/items?limit=5` | Agregaat: top N watchlist-aktsiat hindadega |
| GET | `/api/alerts` | Sessioonipõhised alertid |
| POST | `/api/alerts` | Loo alert (body: `{"symbol","condition":"ABOVE"\|"BELOW","targetPrice"}`); 201 + Location |
| DELETE | `/api/alerts/{id}` | Kustuta alert (omanik-sessioon ainult) |
| GET | `/api/notifications` | Teavitused uuemast vanemani (`?unread=true` filter) |
| GET | `/api/notifications/unread-count` | Lugemata teavituste arv |
| PATCH | `/api/notifications/{id}` | Märgi loetuks |
| POST | `/api/notifications/read-all` | Märgi kõik loetuks |

### Cookie-põhine sessioon

Kõik watchlist-päringud on scope'itud `STOCKSCOPE_SESSION` cookie'le. Esmakordsel päringul filter loob uue UUID-väärtuse ja saadab cookie tagasi (`HttpOnly`, `SameSite=Lax`, 1-aastane). Kaks erinevat sessiooni ei näe teineteise watchlistisid.

**NB!** See on demo-tasandi anonymous session, mitte täis-autentimine. Cookie kopeerimine teisele kasutajale annaks ligipääsu — produktsiooni jaoks vajab eraldi auth-epicit.

### Alerts scheduler

`AlertScheduler` käivitub iga 30 sekundi tagant (`fixedDelay`). Iga tick:
1. `PriceDriftService` rakendab igale mock-aktsiale juhusliku ±1.5% drift'i (NB: vahemikus seatakse päevamuutus dünaamiliselt).
2. `AlertEvaluator` käib läbi kõik aktiivsed alertid ja loob teavituse, kui hind ületab/langeb läbi sihtmäära. Triggerinud alert deaktiveeritakse (üks teavitus per alert).
3. Sessioonipõhine teavituste cap on 100 — uusim sissetulev töötlus kustutab vanimad.

Scheduleri saab testidesse keelata propertyga `stockscope.alerts.scheduler.enabled=false`.

### Testid

```powershell
./mvnw test
```

- Unit testid: JUnit 5 + Mockito (kiire)
- Integration testid (`*IT`): `@SpringBootTest` + Testcontainers (käivitab PostgreSQL Docker'is — vajab Dockerit)
- Repository testid: `@DataJpaTest` H2-ga (PostgreSQL mode)

## Frontend (Vue 3)

**Eeldused:** Node.js 18+, npm.

```powershell
cd frontend
npm install
npm run dev
```

Avaneb aadressil `http://localhost:5173`. Vite proxy suunab `/api` päringud `http://localhost:8080` peale, seega backend peab olema samaaegselt käimas.

### Production build

```powershell
cd frontend
npm run build
```

Väljund kausta `frontend/dist/`.

## Märkmed

- Aktsia-andmed on staatiline mock (`MockStockData.java`) — ühegi reaalse turuallikaga ühenduses pole.
- Watchlistid on persisteeritud PostgreSQL-i (Liquibase changelogs `src/main/resources/db/changelog/`).
- Anonüümne cookie-sessioon, mitte täis-autentimine. CORS lubab `http://localhost:5173` ja `http://localhost:4173` koos `allowCredentials=true`.
- Aktsiate kommentaarid on reegli-põhised (mitte AI).
