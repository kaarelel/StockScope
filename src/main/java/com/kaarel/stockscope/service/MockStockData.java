package com.kaarel.stockscope.service;

import com.kaarel.stockscope.model.RiskLevel;
import com.kaarel.stockscope.model.Stock;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MockStockData {

    private static final List<Stock> STOCKS = List.of(
            // Technology
            new Stock("AAPL", "Apple Inc.", "Technology", 232.41, 230.18, 0.97, 2.13, 4.85, 18.42, 3520, 58, 31.4, 0.45, RiskLevel.LOW,
                    "Apple on globaalne tehnoloogiahiid, mille tuluvood (iPhone, services, wearables) on stabiilsed ja kasvavad."),
            new Stock("MSFT", "Microsoft Corp.", "Technology", 421.83, 418.05, 0.90, 1.45, 3.22, 22.11, 3140, 22, 35.7, 0.72, RiskLevel.LOW,
                    "Microsoft Azure ja AI-tooted toetavad pikaajalist kasvu. Bilanss on tugev, riski peetakse madalaks."),
            new Stock("GOOGL", "Alphabet Inc.", "Technology", 178.92, 176.50, 1.37, 2.88, 5.41, 26.35, 2210, 35, 28.1, 0.00, RiskLevel.LOW,
                    "Alphabet domineerib otsingureklaamis ja Google Cloud kasvab kahekohaliselt."),
            new Stock("NVDA", "NVIDIA Corp.", "Technology", 138.15, 142.30, -2.92, -1.85, 8.74, 142.55, 3380, 410, 65.2, 0.03, RiskLevel.HIGH,
                    "Nvidia on AI-kiipide turuliider. Kõrge volatiilsus, kuid struktuurne kasvulugu jätkub."),
            new Stock("META", "Meta Platforms", "Technology", 612.40, 605.22, 1.19, 3.05, 6.18, 38.74, 1550, 18, 27.3, 0.40, RiskLevel.MEDIUM,
                    "Meta investeerib Reality Labs-i ja AI-sse. Reklaami põhiäri on tugev."),
            new Stock("AMZN", "Amazon.com Inc.", "Technology", 215.66, 213.40, 1.06, 2.14, 4.52, 31.20, 2270, 42, 41.5, 0.00, RiskLevel.MEDIUM,
                    "Amazoni e-kaubandus ja AWS pakuvad kahe-mootorilist kasvu."),
            new Stock("AMD", "Advanced Micro Devices", "Technology", 142.18, 145.80, -2.48, -3.12, -1.45, 24.66, 230, 48, 48.2, 0.00, RiskLevel.HIGH,
                    "AMD konkureerib Inteliga ja kasvab andmekeskuste turul. Volatiilne."),
            new Stock("INTC", "Intel Corp.", "Technology", 22.85, 23.10, -1.08, -2.14, -8.22, -38.45, 98, 75, 0.0, 1.20, RiskLevel.HIGH,
                    "Intel restruktureerib oma valuäri. Lähiaja perspektiiv on ebakindel."),
            new Stock("CRM", "Salesforce Inc.", "Technology", 322.45, 320.10, 0.73, 1.82, 4.11, 14.65, 308, 5, 52.4, 0.40, RiskLevel.MEDIUM,
                    "Salesforce on SaaS CRM-turuliider. AI-tooted (Agentforce) on uus kasvunool."),
            new Stock("ORCL", "Oracle Corp.", "Technology", 184.22, 182.55, 0.92, 2.41, 5.88, 65.32, 510, 12, 39.1, 0.94, RiskLevel.MEDIUM,
                    "Oracle Cloud Infrastructure võidab AI-töökoormustelt. Pikaajaline lepingute kasv."),
            new Stock("ADBE", "Adobe Inc.", "Technology", 502.18, 498.40, 0.76, 1.22, -2.45, -12.18, 222, 3, 41.8, 0.00, RiskLevel.MEDIUM,
                    "Adobe Creative Cloud on tööstusstandard. AI-konkurents avaldab survet."),

            // Finance
            new Stock("JPM", "JPMorgan Chase", "Finance", 245.18, 243.05, 0.88, 1.95, 3.42, 42.18, 695, 9, 12.4, 2.10, RiskLevel.LOW,
                    "JPMorgan on USA suurim pank tugeva bilansiga ja konservatiivse riskikäitumisega."),
            new Stock("BAC", "Bank of America", "Finance", 47.22, 46.80, 0.90, 1.45, 2.88, 38.55, 365, 38, 14.2, 2.65, RiskLevel.LOW,
                    "Bank of America hoiused kasvavad ja netointressimarginaalid on stabiliseerunud."),
            new Stock("WFC", "Wells Fargo", "Finance", 75.18, 74.50, 0.91, 1.32, 3.05, 50.22, 250, 18, 13.1, 2.34, RiskLevel.MEDIUM,
                    "Wells Fargo regulatiivsed piirangud on leevenemas, fookus efektiivsusele."),
            new Stock("GS", "Goldman Sachs", "Finance", 605.40, 600.18, 0.87, 2.10, 4.85, 60.32, 200, 2, 16.8, 2.05, RiskLevel.MEDIUM,
                    "Goldman Sachs investeerimispangandus tugevneb tehingute taastumisega."),
            new Stock("V", "Visa Inc.", "Finance", 318.85, 316.20, 0.84, 1.18, 2.45, 22.18, 615, 7, 32.1, 0.78, RiskLevel.LOW,
                    "Visa makseinfrastruktuur on globaalselt domineeriv. Stabiilne kasv."),
            new Stock("MA", "Mastercard Inc.", "Finance", 545.18, 540.85, 0.80, 1.42, 3.18, 28.65, 510, 3, 38.5, 0.55, RiskLevel.LOW,
                    "Mastercard mahud kasvavad piiriülese liikluse taastudes."),
            new Stock("BRK.B", "Berkshire Hathaway B", "Finance", 462.30, 460.10, 0.48, 0.85, 2.18, 32.41, 998, 4, 9.5, 0.00, RiskLevel.LOW,
                    "Buffett'i konglomeraat hajutatud ettevõtetega. Pikaajaliselt usaldusväärne."),

            // Healthcare
            new Stock("JNJ", "Johnson & Johnson", "Healthcare", 162.45, 161.20, 0.78, 0.95, -1.22, 5.18, 390, 7, 24.5, 3.05, RiskLevel.LOW,
                    "J&J farmaatsia ja meditsiinitehnika ärid pakuvad stabiilsust ja dividendi."),
            new Stock("PFE", "Pfizer Inc.", "Healthcare", 27.85, 28.05, -0.71, -1.45, -3.22, -8.45, 158, 35, 0.0, 6.45, RiskLevel.MEDIUM,
                    "Pfizer COVID-i tulud vähenevad, fookus uutel kasvajavastastel."),
            new Stock("UNH", "UnitedHealth Group", "Healthcare", 528.40, 522.18, 1.19, 2.18, 4.45, -8.18, 488, 5, 22.1, 1.55, RiskLevel.LOW,
                    "UnitedHealth on USA suurim kindlustusandja, regulatiivsed riskid püsivad."),
            new Stock("LLY", "Eli Lilly", "Healthcare", 822.18, 815.40, 0.83, 1.45, 5.22, 28.65, 780, 4, 78.2, 0.65, RiskLevel.MEDIUM,
                    "Eli Lilly GLP-1 ravimid (Mounjaro, Zepbound) juhivad turgu. Hindamine on kõrge."),
            new Stock("MRK", "Merck & Co.", "Healthcare", 102.45, 101.85, 0.59, 0.85, -2.18, -16.45, 260, 11, 22.8, 3.15, RiskLevel.LOW,
                    "Merck Keytruda jätkab dominantsina, kuid patendi-cliff lähemenemas."),

            // Consumer
            new Stock("KO", "Coca-Cola Co.", "Consumer Goods", 65.18, 64.85, 0.51, 0.78, 1.85, 12.18, 280, 14, 26.4, 2.95, RiskLevel.LOW,
                    "Coca-Cola globaalne brand, dividenditõstja. Defensiivne valik."),
            new Stock("PEP", "PepsiCo Inc.", "Consumer Goods", 152.22, 151.40, 0.54, 0.92, 0.45, -8.18, 208, 5, 22.1, 3.55, RiskLevel.LOW,
                    "PepsiCo joogid ja Frito-Lay snäkid pakuvad stabiilset rahavoogu."),
            new Stock("WMT", "Walmart Inc.", "Consumer Goods", 92.18, 91.40, 0.85, 1.45, 3.22, 65.18, 740, 18, 41.2, 1.05, RiskLevel.LOW,
                    "Walmart e-kaubandus kasvab kiiresti, automaatika tõstab marginaale."),
            new Stock("COST", "Costco Wholesale", "Consumer Goods", 945.18, 938.40, 0.72, 1.22, 3.85, 42.55, 420, 2, 56.4, 0.50, RiskLevel.LOW,
                    "Costco liikmemudel toodab korduvtulu. Hindamine on premium."),
            new Stock("MCD", "McDonald's Corp.", "Consumer Goods", 295.18, 293.40, 0.61, 0.95, 1.45, 5.22, 215, 3, 25.1, 2.35, RiskLevel.LOW,
                    "McDonald's globaalne franchise mudel ja menüü optimeerimine. Stabiilne."),
            new Stock("NKE", "Nike Inc.", "Consumer Goods", 78.18, 79.40, -1.54, -2.18, -5.45, -25.18, 118, 9, 22.8, 2.05, RiskLevel.MEDIUM,
                    "Nike kannatab Hiina nõudluse ja konkurentsi tõttu. Käimas on ümberstruktureerimine."),

            // Energy
            new Stock("XOM", "Exxon Mobil", "Energy", 118.45, 117.80, 0.55, 1.05, 2.18, -2.45, 525, 14, 14.2, 3.45, RiskLevel.MEDIUM,
                    "ExxonMobil kasumlik nafta ja gaasi tootja. Tundlik toormehindade suhtes."),
            new Stock("CVX", "Chevron Corp.", "Energy", 158.40, 157.85, 0.35, 0.78, 1.45, -3.18, 290, 8, 14.8, 4.15, RiskLevel.MEDIUM,
                    "Chevron bilanss on tugev. Dividendid atraktiivsed energia-investorile."),
            new Stock("BP", "BP plc", "Energy", 32.18, 32.45, -0.83, -1.22, -4.18, -12.45, 88, 16, 11.5, 5.65, RiskLevel.HIGH,
                    "BP üleminek puhta energia suunas on aeglustunud. Strateegiline ebakindlus."),

            // Industrial
            new Stock("BA", "Boeing Co.", "Industrial", 168.40, 165.85, 1.54, 2.85, 4.18, -22.45, 105, 6, 0.0, 0.00, RiskLevel.HIGH,
                    "Boeing tootmise tagasipöördumine ja kvaliteediprobleemid mõjutavad sentimenti."),
            new Stock("CAT", "Caterpillar Inc.", "Industrial", 365.40, 362.85, 0.70, 1.22, 2.45, 14.18, 178, 3, 17.2, 1.55, RiskLevel.MEDIUM,
                    "Caterpillar tsükliline, kuid infrastruktuuri kulutused toetavad."),
            new Stock("GE", "GE Aerospace", "Industrial", 188.40, 186.85, 0.83, 1.45, 3.22, 38.18, 200, 4, 35.4, 0.65, RiskLevel.MEDIUM,
                    "GE Aerospace jet-mootorite teenused on kõrge marginaali äri."),

            // Communication
            new Stock("DIS", "Walt Disney Co.", "Communication", 105.40, 104.18, 1.17, 2.18, 4.45, -8.18, 192, 9, 32.1, 0.95, RiskLevel.MEDIUM,
                    "Disney+ kasumlikkus paraneb. Pargid pakuvad stabiilset rahavoogu."),
            new Stock("NFLX", "Netflix Inc.", "Communication", 745.18, 738.40, 0.92, 1.85, 4.22, 48.65, 320, 4, 45.2, 0.00, RiskLevel.MEDIUM,
                    "Netflix reklaami-tier ja paroolide jagamise piirangud kiirendasid kasvu."),
            new Stock("T", "AT&T Inc.", "Communication", 23.18, 23.05, 0.56, 0.85, 1.22, 18.45, 165, 25, 18.4, 4.85, RiskLevel.LOW,
                    "AT&T mobiilsidetulud stabiilsed, dividend katab võlga."),

            // Real Estate
            new Stock("AMT", "American Tower", "Real Estate", 215.40, 213.85, 0.72, 1.18, 2.45, 8.22, 100, 2, 38.5, 3.10, RiskLevel.MEDIUM,
                    "American Tower mobiilimastide REIT. Pikaajalised lepingud."),
            new Stock("PLD", "Prologis Inc.", "Real Estate", 122.18, 121.40, 0.64, 1.05, 2.85, 4.18, 113, 3, 29.4, 3.25, RiskLevel.MEDIUM,
                    "Prologis logistikalogistika REIT. E-kaubanduse kasv toetab nõudlust.")
    );

    public List<Stock> all() {
        return STOCKS;
    }
}
