package plugin;

import arc.Core;
import arc.util.CommandHandler;
import arc.util.Timer;
import mindustry.content.Blocks;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.mod.Plugin;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.storage.CoreBlock;

import static arc.util.Strings.canParseInt;
import static plugin.Functions.findByName;
import static plugin.MainEvents.loadEvents;
import static plugin.Menu.loadMenus;

public class Main extends Plugin {

    @Override
    public void init(){
        loadEvents();
        Timer.schedule(()->{
            for (Player player : Groups.player){
                if (player.team() == Team.sharded){
                    Functions.mainThing(player);
                    player.unit().kill();
                }
            }
        }, 0, 10);
    }
    @Override
    public void registerClientCommands(CommandHandler handler){
        handler.<Player>register("merge", "<playername...>", "Merge with other player! (CAN BE DONE ONLY ONCE)", (args,player) ->{
            Player invitedPlayer= findByName(args[0]);
            if (invitedPlayer == null){
                player.sendMessage("This player doesnt exist!");
                return;
            }
            if (invitedPlayer == player){
                player.sendMessage("You cant merge yourself!");
                return;
            }
            if (plugin.Vars.mergedPlayers.contains(invitedPlayer.uuid()) || plugin.Vars.mergedPlayers.contains(player.uuid())){
                player.sendMessage("You cant merge with anyone who already merged!");
                return;
            }
            loadMenus(invitedPlayer, player, player.team(), invitedPlayer.team(), plugin.Vars.teams);
            player.sendMessage("[green]Request sent!");
        });
        handler.<Player>register("recoverbase", "<team> <radius>", "Recover base of players!", (args, players) -> {
            if (!players.admin()){
                players.sendMessage("You are not admin!");
                return;
            }
            if (!canParseInt(args[0]) ||!canParseInt(args[1])){
                players.sendMessage("Cant parse int! type a number");
                return;
            }
            Team team = Team.get(Integer.parseInt(args[0]));
            int radius = Integer.parseInt(args[1]);
            for (int x = players.tileX() - radius; x <= players.tileX()  + radius; x++)
                for (int y = players.tileY()  - radius; y <= players.tileY()  + radius; y++){
                    if (mindustry.Vars.world.tile(x,y) == null){
                        return;
                    }
                    if (mindustry.Vars.world.tile(x, y).build instanceof CoreBlock.CoreBuild && mindustry.Vars.world.tile(x,y).build.team.id == 255) {
                        Building core = mindustry.Vars.world.tile(x, y).build;
                        int buildX = core.tileX();
                        int buildY = core.tileY();
                        Tile tile = mindustry.Vars.world.tile(buildX, buildY);
                        if(!mindustry.Vars.net.client()){
                            tile.setBlock(core.block, team);
                        }
                        Core.app.post(() -> tile.setNet(core.block, team, 0));
                    }
                    if (mindustry.Vars.world.tile(x, y).build != null && !(mindustry.Vars.world.tile(x,y).build instanceof CoreBlock.CoreBuild)){
                        Tile tile = mindustry.Vars.world.tile(x, y);
                        Timer.schedule(() -> Call.setTeam(tile.build, team), 0.3f);
                        }
                }
            players.sendMessage("[green]Base has been recovered");
        });
        handler.<Player>register("plrinfo", "<playername...>", "Get information about player", (args, player) -> {
            Player plr = findByName(args[0]);
            if (plr == null){
                player.sendMessage("No such player!");
                return;
            }
            int index = Vars.players.indexOf(plr.uuid());
            Team team = Team.get(Vars.teams.get(index));
            player.sendMessage(plr.plainName() + "[orange], TEAM: #" + team.id);
        });
        handler.<Player>register("unmerge", "<playername...>", "unmerge player", (args, player) -> {
            if (!player.admin()){
                player.sendMessage("You are not admin!");
                return;
            }
            Player plr = findByName(args[0]);
            if (plr == null){
                player.sendMessage("No such player!");
                return;
            }
            Vars.mergedPlayers.remove(plr.uuid());
            player.sendMessage("[green]Successfully removed merge effect!");
        });
        handler.<Player>register("team", "<playername> <team>", "unmerge player", (args, player) -> {
            if (!player.admin()){
                player.sendMessage("You are not admin!");
                return;
            }
            if (!canParseInt(args[1])){
                player.sendMessage("Cant parse int! type a number");
                return;
            }
            Player plr = findByName(args[0]);
            Team team = Team.get(Integer.parseInt(args[1]));
            plr.team(team);
            int index = Vars.players.indexOf(plr.uuid());
            Vars.teams.set(index, team.id);
        });
    }
}