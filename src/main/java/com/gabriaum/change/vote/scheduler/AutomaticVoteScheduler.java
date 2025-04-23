package com.gabriaum.change.vote.scheduler;

import com.gabriaum.change.ChangeMain;
import com.gabriaum.change.type.ChangeType;
import com.gabriaum.change.vote.Vote;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class AutomaticVoteScheduler extends BukkitRunnable {

    @Override
    public void run() {

        Vote vote = ChangeMain.getInstance().getVote();

        FileConfiguration configuration = ChangeMain.getInstance().getConfig();

        List<Player> players = getWorlds().stream().map(World::getPlayers).findFirst().orElse(null);

        if (players == null || players.isEmpty()) {
            return;
        }

        if (!vote.isVoting()) {

            if (vote.isAutomatic()) {

                int count = vote.getCount();

                count--;

                if (count <= 0) {

                    vote.setVoting(true);
                    vote.setDeadlineCount(configuration.getInt("vote.scheduler.deadline"));

                    return;
                }

                vote.setCount(count);
            }

            return;
        }

        int deadlineCount = vote.getDeadlineCount();

        if (deadlineCount <= 0) {

            vote.setDeadlineCount(0);
            vote.setCount(configuration.getInt("vote.scheduler.delay"));
            vote.setVoting(false);

            sendMessage(ChatColor.RED + "The vote has ended!");

            if (vote.isAccepted()) {

                ChangeType expectedType = vote.getExpectedType();

                vote.setActualType(expectedType);
                vote.setExpectedType(ChangeType.getNext(expectedType));

                expectedType.getExecutor().execute(getWorlds());

                sendMessage(ChatColor.GREEN + "The vote has been accepted!");
                sendMessage(ChatColor.GOLD + "The weather will change to " + ChatColor.AQUA + expectedType.name() + ChatColor.GOLD + "!");
            } else if (vote.isUnanimous()) {

                sendMessage(ChatColor.YELLOW + "The vote was unanimous!");
            } else {

                sendMessage(ChatColor.RED + "The vote has been denied!");
            }

            vote.getDenied().clear();
            vote.getAccepted().clear();

            return;
        }

        if (deadlineCount % 10 == 0 || deadlineCount == 5) {

            sendVoteMessage(vote);
        }

        deadlineCount--;
        vote.setDeadlineCount(deadlineCount);
    }

    protected void sendVoteMessage(Vote vote) {

        FileConfiguration configuration = ChangeMain.getInstance().getConfig();

        List<World> worlds = new ArrayList<>(configuration.getStringList("vote.worlds").stream()
                .map(Bukkit::getWorld)
                .toList());

        if (worlds.isEmpty()) {
            worlds.add(Bukkit.getWorlds().get(0));
        }

        for (World world : worlds) {

            world.getPlayers().forEach(player -> {

                player.sendMessage(" ");
                player.sendMessage(ChatColor.GOLD + "A vote to change the weather to " + ChatColor.AQUA + vote.getExpectedType() + ChatColor.GOLD + " has started!");
                player.sendMessage(ChatColor.GRAY + "Type " + ChatColor.YELLOW + "/climatepool vote yes" + ChatColor.GRAY + " or " + ChatColor.YELLOW + "/climatepool vote no" + ChatColor.GRAY + " to cast your vote.");
                player.sendMessage(ChatColor.GRAY + "You have " + ChatColor.GREEN + vote.getDeadlineCount() + " seconds" + ChatColor.GRAY + " to vote!");
                player.sendMessage(" ");
            });
        }
    }

    protected void sendMessage(String message) {

        List<World> worlds = new ArrayList<>(ChangeMain.getInstance().getConfig().getStringList("vote.worlds").stream()
                .map(Bukkit::getWorld)
                .toList());

        if (worlds.isEmpty()) {
            worlds.add(Bukkit.getWorlds().get(0));
        }

        for (World world : worlds) {

            world.getPlayers().forEach(player -> player.sendMessage(message));
        }
    }

    public static List<World> getWorlds() {

        List<World> worlds = new ArrayList<>(ChangeMain.getInstance().getConfig().getStringList("vote.worlds").stream()
                .map(Bukkit::getWorld)
                .toList());

        if (worlds.isEmpty()) {
            worlds.add(Bukkit.getWorlds().get(0));
        }

        return worlds;
    }
}