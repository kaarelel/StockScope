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

- **Dashboard** — turu üldseis, indeksid, top tõusjad/langejad, aktiivsed
- **Aktsiad** — täielik tabel otsingu, sortimise ja sektorifiltriga (~40 aktsiat 8 sektoris)
- **Stock detail** — hind, muutused, P/E, dividend, ülevaade ja automaatne kommentaar
- **Portfell** — kujuteldav investeering, sektorite jaotus, tootlus, üldine risk
- **Turuülevaated** — demo-trendid ja sektoripõhised tähelepanekud

## Backend (Spring Boot)

**Eeldused:** Java 17+, Maven (või kasuta `mvnw`).

```powershell
./mvnw spring-boot:run
```

Käivitub aadressil `http://localhost:8080`. Demo-andmed on mock-failina koodi sees, andmebaasi pole vaja.

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

- Andmed on staatiline mock (`MockStockData.java`) — ühegi reaalse turuallikaga ühenduses pole.
- Sisselogimist ei ole. CORS lubab `http://localhost:5173` ja `http://localhost:4173`.
- Aktsiate kommentaarid on reegli-põhised (mitte AI). `spring-ai` sõltuvus eemaldati.
