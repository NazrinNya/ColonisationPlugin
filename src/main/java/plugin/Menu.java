package plugin;

import arc.struct.Seq;
import arc.util.Timer;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.ui.Menus;

import static plugin.Functions.coreCapture;
import static plugin.Vars.mergedPlayers;

public class Menu {
    public static void loadMenus(Player invitedPlayer, Player invitingPlayer, Team mergeTeam, Team mergingTeam, Seq<Integer> teams) {
        String mergeInvite = invitingPlayer.plainName() + " Wants to merge with you! Do you agree?\n" +
                "[#f]WARNING: Make sure to know what are you doing! You cant cancel this action once you agreed and you can merge only once!";
        int mergeMenu = Menus.registerMenu((playerInMenu, option) -> {
            switch (option) {
                case -1 -> {
                    return;
                }
                case 0 -> {
                    invitedPlayer.team(mergeTeam);
                    Groups.unit.each(u -> u.team == mergingTeam, unit -> unit.team = mergeTeam);
                    int index = teams.indexOf(mergingTeam.id);
                    teams.set(index, mergeTeam.id);
                    mergedPlayers.add(invitedPlayer.uuid());
                    mergedPlayers.add(invitingPlayer.uuid());
                    Timer.schedule(() -> {
                        coreCapture(mergingTeam, mergeTeam);
                    }, 0, 1, 5);
                    Call.announce(mergeTeam + " And " + mergingTeam +" just merged!");
                }
                case 1 -> {
                    invitingPlayer.sendMessage("[red]" + invitedPlayer.name() + "[red] Declined your merge req!");
                    return;
                }
            }
        });
        Call.menu(invitedPlayer.con(), mergeMenu, "Invitation", mergeInvite, new String[][]{{"[green]Agree", "[red]Decline"}});
    }
}
