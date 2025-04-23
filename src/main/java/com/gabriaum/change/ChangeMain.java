package com.gabriaum.change;

import com.gabriaum.change.command.data.CommandFramework;
import com.gabriaum.change.command.impl.ClimateCommand;
import com.gabriaum.change.type.ChangeType;
import com.gabriaum.change.vote.Vote;
import com.gabriaum.change.vote.scheduler.AutomaticVoteScheduler;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ChangeMain extends JavaPlugin {

    @Getter
    protected static ChangeMain instance;

    private Vote vote;

    private CommandFramework commandFramework;
    private AutomaticVoteScheduler automaticVoteScheduler;

    @Override
    public void onLoad() {
        saveDefaultConfig();

        instance = this;
        vote = new Vote(ChangeType.valueOf(getConfig().getString("initial-change")));
    }

    @Override
    public void onEnable() {

        try {
            handleWorld();
        } catch (Exception ex) {
            getLogger().severe("Error while handling world: " + ex.getMessage());
            getServer().shutdown();
            return;
        }

        commandFramework = new CommandFramework(this);
        automaticVoteScheduler = new AutomaticVoteScheduler();

        commandFramework.registerCommands(
                new ClimateCommand()
        );

        vote.setAutomatic(getConfig().getBoolean("vote.scheduler.automatic"));

        automaticVoteScheduler.runTaskTimer(this, 0, 20L);
    }

    protected void handleWorld() {

        List<World> worlds = new ArrayList<>(ChangeMain.getInstance().getConfig().getStringList("vote.worlds").stream()
                .map(Bukkit::getWorld)
                .toList());

        if (worlds.isEmpty()) {
            worlds.add(Bukkit.getWorlds().get(0));
        }

        for (World world : worlds) {

            if (world == null) {
                continue;
            }

            world.setGameRuleValue("doWeatherCycle", "false");
        }
    }
}