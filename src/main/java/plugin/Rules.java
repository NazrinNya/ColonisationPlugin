package plugin;

import mindustry.Vars;
import mindustry.content.UnitTypes;

public class Rules {
    public static void loadRulesAndEtc(){
        plugin.Vars.players.clear();
        plugin.Vars.teams.clear();
        plugin.Vars.mergedPlayers.clear();
        Vars.state.rules.canGameOver = false;
        Vars.state.rules.coreCapture = true;
        UnitTypes.nova.weapons.each(w -> w.bullet.buildingDamageMultiplier = 0.01f);
        UnitTypes.pulsar.weapons.each(w -> w.bullet.buildingDamageMultiplier = 0.01f);
        UnitTypes.quasar.weapons.each(w -> w.bullet.buildingDamageMultiplier = 0.01f);
        UnitTypes.poly.weapons.each(w -> w.bullet.buildingDamageMultiplier = 0.01f);
        UnitTypes.mega.weapons.each(w -> w.bullet.buildingDamageMultiplier = 0.01f);
    }
}
