package plugin;


import arc.Core;
import arc.Events;
import arc.util.*;
import mindustry.content.Blocks;
import mindustry.content.UnitTypes;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.gen.Unit;
import mindustry.world.Tile;
import mindustry.world.blocks.storage.CoreBlock;

import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import static plugin.Functions.*;
import static plugin.Rules.loadRulesAndEtc;

public class MainEvents {
    public static void loadEvents(){
        Events.on(EventType.BlockDestroyEvent.class, event -> {
            if (event.tile.block() instanceof CoreBlock) {
                for (int teamId : plugin.Vars.teams){
                    Timer.schedule(() -> {
                        if (Team.get(teamId).cores().size == 0){
                            Functions.coreCapture(Team.get(teamId), event.tile.team());
                        }
                    }, 1f);
                }
            }
        });
        Timer.schedule(() -> {
            Call.infoPopup("Use shock mines next to walls to break them!\n"
                            +"Используйте шоковые мины рядом со стенами чтобы ломать их",
                    1, Align.top| Align.left, 0,0,0,0);
        }, 0, 1);
        Timer.schedule(() -> {
            for (Unit fortress : Groups.unit){
                /*if (fortress.type == UnitTypes.fortress || fortress.type == UnitTypes.bryde || fortress.type == UnitTypes.spiroct){
                    wallRemoval((int) (fortress.x/8), (int) (fortress.y/8), 1, 1);
                }*/
                if (fortress.type == UnitTypes.reign || fortress.type == UnitTypes.toxopid){
                   wallRemoval((int) (fortress.x/8), (int) (fortress.y/8), 2, 2);
                }
            }
        }, 0, 5);
        Events.on(EventType.WorldLoadEndEvent.class, event ->{
            loadRulesAndEtc();
            java.util.Timer gameoverTimer = new java.util.Timer();
            AtomicInteger i = new AtomicInteger(10800);
            gameoverTimer.schedule((new TimerTask() {
                @Override
                public void run() {
                    i.getAndAdd(-1);
                    Call.infoPopup("Time until map reset -> " + Strings.formatMillis(i.get()* 1000L), 1, Align.bottom | Align.center, 0,0,0,0);
                    Events.on(EventType.GameOverEvent.class, gameevent-> {
                        gameoverTimer.cancel();
                    });
                    if (i.get() <= 0){
                        Events.fire(new EventType.GameOverEvent(Team.derelict));
                    }
                }
            }), 0, 1000);
            Events.on(EventType.GameOverEvent.class, gameevent-> {
                gameoverTimer.cancel();
            });
        });
        Events.on(EventType.BlockBuildEndEvent.class, event ->{
            if (event.tile.block() == Blocks.shockMine) {
                wallRemoval(event.tile.x, event.tile.y, 1, 1);
                Timer.schedule(()-> {
                    mindustry.Vars.world.tile(event.tile.x, event.tile.y).setNet(Blocks.air);
                }, 0.1f);
            }
        });
        Events.on(EventType.PlayerJoin.class, event ->{
            Functions.mainThing(event.player);
        });
        Events.on(EventType.PlayerLeave.class, event -> {
            java.util.Timer timer = new java.util.Timer();
            AtomicInteger i = new AtomicInteger(290);

            timer.schedule((new TimerTask() {
                @Override
                public void run() {
                    i.getAndAdd(-1);
                    int index = Vars.players.indexOf(event.player.uuid());
                    if (index == -1){
                        Log.info("Nah");
                        timer.cancel();
                        return;
                    }
                    Team team = Team.get(Vars.teams.get(index));
                    if (team.data().players.size >= 1){
                        Log.info("Player size is more or equal 1, no");
                        timer.cancel();
                        return;
                    }
                    Player player = Groups.player.find(p -> p.uuid().equals(event.player.uuid()));
                    if (player == null){
                        if (i.get() <= 0){
                            Timer.schedule(() -> {
                                coreCapture(team, Team.get(255));
                            }, 0, 1f, 3);
                            timer.cancel();
                        }
                    }
                }
            }), 100, 1000);
        });
    }

}
