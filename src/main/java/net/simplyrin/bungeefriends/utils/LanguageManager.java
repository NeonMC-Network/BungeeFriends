package net.simplyrin.bungeefriends.utils;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.YamlConfiguration;
import net.simplyrin.bungeefriends.Main;
import net.simplyrin.bungeefriends.tools.Config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by SimplyRin on 2018/08/29.
 * <p>
 * Copyright (c) 2018 SimplyRin
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
public class LanguageManager {

    private Main plugin;

    private HashMap<String, Configuration> configMap;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public LanguageManager(Main plugin) {
        this.plugin = plugin;
        this.configMap = new HashMap<>();

        File folder = LanguageManager.this.plugin.getDataFolder();
        if (!folder.exists()) {
            folder.mkdir();
        }

        File languageFolder = new File(folder, "Language");
        if (!languageFolder.exists()) {
            languageFolder.mkdir();
        }

        String[] langs = {"english", "japanese", "chinese", "arabic", "romanian", "spanish", "russian", "french", "hungarian"};
        for (String lang : langs) {
            File languageFile = new File(languageFolder, lang + ".yml");
            if (!languageFile.exists()) {
                try {
                    InputStream inputStream = LanguageManager.this.plugin.getResourceAsStream(lang + ".yml");
                    FileOutputStream outputStream = new FileOutputStream(languageFile);
                    //noinspection UnstableApiUsage
                    ByteStreams.copy(inputStream, outputStream);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public LanguageUtils getPlayer(ProxiedPlayer player) {
        return new LanguageUtils(player.getUniqueId());
    }

    public LanguageUtils getPlayer(UUID uniqueId) {
        return new LanguageUtils(uniqueId);
    }

    public class LanguageUtils {

        private UUID uuid;
        private Configuration data;

        LanguageUtils(UUID uuid) {
            this.uuid = uuid;
            try {
                this.data = YamlConfiguration.getProvider(YamlConfiguration.class).load(new File(Main.playerDataFolder, uuid + ".yml"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            Object lang = LanguageManager.this.plugin.getString("Player." + this.uuid.toString() + ".Language");
            if (lang == null || lang.equals("")) {
                LanguageManager.this.plugin.set("Player." + this.uuid.toString() + ".Language", "english");
                LanguageManager.this.configMap.put("english", Config.getConfig(this.getFile("english"), Charsets.UTF_8));
            }
        }

        public String getLanguage() {
            if (data != null) {
                String language = formatLanguage(data.getString("language"));
                setLanguage(language);
                return language;
            }
            String key = LanguageManager.this.plugin.getString("Player." + this.uuid.toString() + ".Language");
            if (key == null || key.equals("")) {
                return "english";
            }
            return key.substring(0, 1).toUpperCase() + key.substring(1);
        }

        // from "xx-YY" to "xxxxx"
        private String formatLanguage(String input) {
            if (input.contains("en"))
                return "english";
            if (input.contains("zh"))
                return "chinese";
            return "english";
        }

        public void setLanguage(String key) {
            LanguageManager.this.plugin.set("Player." + this.uuid.toString() + ".Language", key);
        }

        public String getString(String configKey) {
            Configuration config = LanguageManager.this.configMap.get(this.getLanguage());

            if (config == null) {
                File file = new File(this.getLanguagesFolder(), this.getLanguage().toLowerCase() + ".yml");
                LanguageManager.this.configMap.put(this.getLanguage(), Config.getConfig(file, Charsets.UTF_8));
            }

            config = LanguageManager.this.configMap.get(this.getLanguage());
            String result = config.getString(configKey);
            if (result.equals("")) {
	            try {
		            LanguageManager.this.configMap.get("english").getString(configKey);
	            } catch (Exception e) {
		            LanguageManager.this.plugin.info("&cAn error has occurred! You need remove 'english.yml' or set '" + configKey + "'!");
		            e.printStackTrace();
	            }
            }
            return result;
        }

        @SuppressWarnings("ResultOfMethodCallIgnored")
        File getLanguagesFolder() {
            File folder = LanguageManager.this.plugin.getDataFolder();
            if (!folder.exists()) {
                folder.mkdir();
            }

            File languageFolder = new File(folder, "Language");
            if (!languageFolder.exists()) {
                languageFolder.mkdir();
            }

            return languageFolder;
        }

        public File getFile() {
            return this.getFile(this.getLanguage());
        }

        File getFile(String key) {
            return new File(this.getLanguagesFolder(), key.toLowerCase() + ".yml");
        }

    }

}
