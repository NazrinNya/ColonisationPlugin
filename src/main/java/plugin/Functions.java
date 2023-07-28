package plugin;

import arc.Core;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Threads;
import arc.util.Timer;
import mindustry.content.Blocks;
import mindustry.content.UnitTypes;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.world.Tile;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.world.modules.ItemModule;

import static mindustry.Vars.tilesize;

public class Functions {
    public static void wallRemoval(int rx, int ry, int xRadius, int yRadius){
        for (int x = rx - xRadius; x <= rx + xRadius; x++)
            for (int y = ry - yRadius; y <= ry + yRadius; y++) {
                if (mindustry.Vars.world.tile(x,y) == null){
                    return;
                }
                if (mindustry.Vars.world.tile(x, y).block().isHidden()) {
                    mindustry.Vars.world.tile(x, y).setNet(Blocks.air);
                }
                if (mindustry.Vars.world.tile(x, y).floor() == Blocks.tar.asFloor()) {
                    oilDetected(x,y);
                }
            }
    }
    public static void oilDetected(int rx, int ry){
        for (int x = rx - 10; x <= rx + 10; x++)
            for (int y = ry - 10; y <= ry + 10; y++){
                if (mindustry.Vars.world.tile(x,y) == null){
                    return;
                }
                if (mindustry.Vars.world.tile(x,y).floor() == Blocks.tar.asFloor() && mindustry.Vars.world.tile(x,y).block().isHidden()){
                    mindustry.Vars.world.tile(x,y).setNet(Blocks.air);
                }
            }
    }
    public static void mainThing(Player player){
        final int[] randomx = {Mathf.random(25, mindustry.Vars.state.map.width - 25)};
        final int[] randomy = {Mathf.random(25, mindustry.Vars.state.map.height - 25)};
        final int[] randomTeam = {Mathf.random(2, 250)};
        int ii = 0;
        int i = 0;
        while(ii++ < 250 && Team.get(randomTeam[0]).active()){
            randomTeam[0] = Mathf.random(2,250);
        }
        while(i++ < 250 && mindustry.Vars.state.teams.getActive().contains(team -> team.cores.contains(core -> core.dst(randomx[0] * tilesize, randomy[0] * tilesize) < 75 * tilesize))){
            Log.info("Too near, retrying...");
            randomx[0] = Mathf.random(25, mindustry.Vars.state.map.width - 25);
            randomy[0] = Mathf.random(25, mindustry.Vars.state.map.height - 25);
        }
        int index = Vars.players.indexOf(player.uuid());
        if (index < 0) {
            randomThing(randomx[0], randomy[0], randomTeam[0],player);
            return;
        }
        if (Vars.players.get(index).equals(player.uuid())){
            if (Team.get(Vars.teams.get(index)).cores().size == 0L) {
                Vars.players.remove(index);
                Vars.teams.remove(index);
                try {
                    Vars.mergedPlayers.remove(player.uuid());
                } catch (Exception ignored){}
                randomThing(randomx[0], randomy[0], randomTeam[0],player);
                return;
            }
            player.team(Team.get(Vars.teams.get(index)));
            return;
        }
        randomThing(randomx[0], randomy[0], randomTeam[0],player);
    }

    public static void randomThing(int randomX, int randomY, int randomTeam, Player eventPlayer){
        for (int x = randomX; x <= randomX + 19; x++)
            for (int y = randomY; y <= randomY + 19; y++){
                if (mindustry.Vars.world.tile(x,y) == null){
                    return;
                }
                if (mindustry.Vars.world.tile(x, y).block().isHidden()) {
                    mindustry.Vars.world.tile(x, y).setNet(Blocks.air); mindustry.Vars.world.tile(x,y).setFloorNet(Blocks.darksand);

                }
            }
        mindustry.Vars.world.tile(randomX+10, randomY+10).setNet(Blocks.coreNucleus, Team.get(randomTeam), 0);
        Team.get(randomTeam).core().items.add(mindustry.Vars.content.item(0), 700);
        Team.get(randomTeam).core().items.add(mindustry.Vars.content.item(1), 1000);
        Team.get(randomTeam).core().items.add(mindustry.Vars.content.item(3), 500);
        Team.get(randomTeam).core().items.add(mindustry.Vars.content.item(9), 500);
        Vars.players.add(eventPlayer.uuid());
        Vars.teams.add(randomTeam);
        mindustry.Vars.state.rules.teams.get(Team.get(randomTeam)).rtsAi = true;
        eventPlayer.team(Team.get(randomTeam));
        UnitTypes.dagger.spawn(Team.get(randomTeam), randomX * 8 + 32, randomY * 8 + 32);
        for (int x = randomX; x <= randomX + 4; x++)
            for (int y = randomY; y <= randomY + 4; y++){
                var tile = mindustry.Vars.world.tile(x,y);
                Call.setOverlay(tile, Blocks.oreLead);
            }
        for (int x = randomX + 15; x <= randomX + 19; x++)
            for (int y = randomY; y <= randomY + 4; y++){
                var tile = mindustry.Vars.world.tile(x,y);
                Call.setOverlay(tile, Blocks.oreCoal);
            }
        for (int x = randomX; x <= randomX + 19; x++)
            for (int y = randomY + 15 ; y <= randomY + 19; y++){
                var tile = mindustry.Vars.world.tile(x,y);
                Call.setOverlay(tile, Blocks.oreCopper);
            }
    }

    public static void coreCapture(Team prTeam, Team team){
        var builds = new Seq<Building>();
        if (prTeam.data().buildingTree != null){
            prTeam.data().buildingTree.getObjects(builds);
        }
        try {
            ItemModule items = prTeam.core().items;
            team.core().items.add(items);
        } catch (Exception e){
            Log.info("Items is null, not adding");
        }
            for (var b : builds) {
                for (CoreBlock.CoreBuild core : prTeam.cores()) {
                    Tile tile = mindustry.Vars.world.tile((int) core.x/8, (int) core.y/8);
                    if(!mindustry.Vars.net.client()){
                        tile.setBlock(core.block, team);
                    }
                    Core.app.post(() -> tile.setNet(core.block, team, 0));
                }
                Timer.schedule(() -> Call.setTeam(b, team), 0.3f);
                Groups.unit.each(u -> u.team() == prTeam, u -> u.team = team);
            }
        }
   /*public static void forceSync(Team playerTeam, int delay){
       Timer.schedule(() -> {
           for (Player player : Groups.player){
               if (player.team() == playerTeam){
                   Call.worldDataBegin(player.con);
                   mindustry.Vars.netServer.sendWorldData(player);
               }
           }
       }, delay);*/
   public static void coreCaptureNoRes(Team prTeam, Team team){
       var builds = new Seq<Building>();
       if (prTeam.data().buildingTree != null){
           prTeam.data().buildingTree.getObjects(builds);
       }
   }
   public static Player findByName(String name){
       return Groups.player.find(p -> p.plainName().contains(name));
   }
   }