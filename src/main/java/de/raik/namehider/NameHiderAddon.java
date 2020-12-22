package de.raik.namehider;

import com.google.gson.JsonObject;
import de.raik.namehider.command.CommandDispatcher;
import de.raik.namehider.implementation.HiderCoreImplementation;
import de.raik.namehider.settingelements.ButtonElement;
import de.raik.namehider.settingelements.DescribedBooleanElement;
import net.labymod.api.LabyModAddon;
import net.labymod.core.LabyModCore;
import net.labymod.settings.Settings;
import net.labymod.settings.elements.BooleanElement;
import net.labymod.settings.elements.ControlElement;
import net.labymod.settings.elements.SettingsElement;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Addon instance
 * to manage the addon
 *
 * @author Raik
 * @version 1.0
 */
public class NameHiderAddon extends LabyModAddon {

    /**
     * If true names will show
     */
    private boolean showNames = true;

    /**
     * Display configuration of the whole addon
     */
    private final DisplayConfiguration configuration = new DisplayConfiguration(this, false);

    /**
     * Command handling class in the addon
     */
    private CommandDispatcher commandDispatcher;

    /**
     * Executor service for executing code async
     */
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    /**
     * Init method called by
     * the addon api to setup the addon
     */
    @Override
    public void onEnable() {
        //Setting core adapter to apply the changes to the rendering
        LabyModCore.setCoreAdapter(new HiderCoreImplementation(this));

        //Creating command dispatcher
        commandDispatcher = new CommandDispatcher(this);
    }

    /**
     * Method called by api
     * to load setting from config file
     */
    @Override
    public void loadConfig() {
        JsonObject addonConfig = this.getConfig();

        //General Settings
        this.showNames = addonConfig.has("shownames") ? addonConfig.get("shownames").getAsBoolean() : this.showNames;

        //Configuration
        if (addonConfig.has("yellowpen"))
            this.configuration.setYellowPen(addonConfig.get("yellowpen").getAsBoolean());
        if (addonConfig.has("ranks"))
            this.configuration.setRanks(addonConfig.get("ranks").getAsBoolean());
        if (addonConfig.has("subtitles"))
            this.configuration.setSubtitles(addonConfig.get("subtitles").getAsBoolean());
        if (addonConfig.has("scoreboards"))
            this.configuration.setScoreboards(addonConfig.get("scoreboards").getAsBoolean());

        //Commands
        this.commandDispatcher.loadConfig();
    }

    /**
     * Method called by the addon api
     * for adding settings to the addon config
     *
     * @param settings The list of setting elements
     */
    @Override
    protected void fillSettings(List<SettingsElement> settings) {
        //Settings

        //Setting for the show Names property setting the value
        BooleanElement showNamesElement = new BooleanElement("Show player names"
                , new ControlElement.IconData("labymod/textures/settings/settings/showmyname.png")
                , changeValue -> this.showNames = changeValue, this.showNames);
        Settings showNamesSubSettings = showNamesElement.getSubSettings();

        //Configurations
        showNamesSubSettings.add(new DescribedBooleanElement("Yellow pen", this
                , new ControlElement.IconData("labymod/addons/namehider/yellow_pen.png"), "yellowpen", this.configuration.isYellowPen()
                , "If disabled, the yellow pen next to custom name tags will be hidden", this.getConfig(), false)
                .addCallback(this.configuration::setYellowPen));
        showNamesSubSettings.add(new DescribedBooleanElement("Ranks", this
                , new ControlElement.IconData("labymod/textures/misc/crown.png"), "ranks", this.configuration.isRanks()
                , "If disabled, rank badges or tags won't show next to the player name", this.getConfig(), false)
                .addCallback(this.configuration::setRanks));
        showNamesSubSettings.add(new DescribedBooleanElement("Subtitles", this
                , new ControlElement.IconData("labymod/addons/namehider/subtitles.png"), "subtitles", this.configuration.isSubtitles()
                , "If disabled, the subtitle set by the servers won't show.", this.getConfig(), false)
                .addCallback(this.configuration::setSubtitles));
        showNamesSubSettings.add(new DescribedBooleanElement("Scoreboards", this
                , new ControlElement.IconData("labymod/textures/settings/modules/scoreboard_background.png"), "scoreboards"
                , this.configuration.isScoreboards(), "If disabled, scoreboards shown below the name will be hidden"
                , this.getConfig(), false).addCallback(this.configuration::setScoreboards));

        settings.add(showNamesElement);

        //Button element to save the config
        settings.add(new ButtonElement("Save Config", new ControlElement.IconData("labymod/textures/buttons/update.png")
                , buttonElement -> executorService.execute(() -> {
                    buttonElement.setEnabled(false);
                    this.saveConfiguration();
                    buttonElement.setEnabled(true);
        }), "Save", "Saves the config made as default."));

        //Commands
        this.commandDispatcher.addCommandSettings(settings);
    }

    /**
     * Method to save
     * the default configuration
     */
    private void saveConfiguration() {
        JsonObject addonConfig = this.getConfig();

        //Set properties
        addonConfig.addProperty("shownames", this.showNames);
        addonConfig.addProperty("yellowpen", this.configuration.isYellowPen());
        addonConfig.addProperty("ranks", this.configuration.isRanks());
        addonConfig.addProperty("subtitles", this.configuration.isSubtitles());
        addonConfig.addProperty("scoreboards", this.configuration.isScoreboards());

        //Saving
        this.saveConfig();
        this.loadConfig();
    }

    /**
     * Method to get a user specific configuration
     * returning default if the user hasn't any
     *
     * @param name The user name
     * @return The configuration
     */
    public DisplayConfiguration receiveConfigurationForPlayer(String name) {
        return this.configuration;
    }

    /**
     * Getter for showNames
     * protected for only letting DisplayConfiguration
     * access
     *
     * @return The value
     */
    protected boolean isShowNames() {
        return showNames;
    }
}
