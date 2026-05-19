package com.kaarel.stockscope.service;

import com.kaarel.stockscope.model.MarketInsight;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InsightsService {

    private static final List<MarketInsight> INSIGHTS = List.of(
            new MarketInsight(
                    "AI-kiipide nõudlus jätkab kasvu",
                    "Tehnoloogia",
                    "Andmekeskuste investeeringud globaalselt kasvavad, mis tõstab nõudlust spetsialiseeritud GPU-de järele.",
                    "Positiivne sektorile: pooljuhid, pilv-infrastruktuur."),
            new MarketInsight(
                    "Intressimäärad stabiliseeruvad",
                    "Makro",
                    "Föderaalreserv signaliseerib, et inflatsioon liigub eesmärgi suunas. Pikaajalised intressid tasapisi langevad.",
                    "Positiivne: kasvuaktsiad, kinnisvara-REIT-id. Neutraalne: pangad."),
            new MarketInsight(
                    "GLP-1 ravimid muudavad farmaatsia maastikku",
                    "Tervishoid",
                    "Rasvumisravimid (Mounjaro, Zepbound, Wegovy) tõstavad valitud farmaatsia ettevõtete tulusid kahekohaliselt.",
                    "Positiivne: Eli Lilly, Novo Nordisk. Risk: konkurentsi süvenemine."),
            new MarketInsight(
                    "Hiina nõudluse aeglustumine mõjutab tarbekaupu",
                    "Tarbijasektor",
                    "Hiina majanduskasvu aeglustumine survestab globaalseid tarbijabrände, eriti luksuskaupade ja jalanõude segmendis.",
                    "Negatiivne: Nike, LVMH, Estée Lauder."),
            new MarketInsight(
                    "Rohelise energia tagasilöök",
                    "Energia",
                    "Naftafirmad kärbivad puhta energia investeeringuid, kuna fossiilkütuste kasumlikkus on jäänud kõrgele.",
                    "Neutraalne suurtele integreeritud nafta-firmadele. Negatiivne puhta energia ettevõtetele."),
            new MarketInsight(
                    "E-kaubanduse logistikanõudlus tõuseb",
                    "Kinnisvara",
                    "Hoiu- ja jaotuskeskuste vajadus kasvab e-kaubanduse kasvuga, toetades logistika-REIT-ide tulusid.",
                    "Positiivne: Prologis, STAG Industrial.")
    );

    public List<MarketInsight> all() {
        return INSIGHTS;
    }
}
