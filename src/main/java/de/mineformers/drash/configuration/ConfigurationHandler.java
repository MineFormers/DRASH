package de.mineformers.drash.configuration;

import java.io.File;
import java.util.logging.Level;

import net.minecraftforge.common.Configuration;
import cpw.mods.fml.common.FMLLog;
import de.mineformers.drash.DRASH;

/**
 * Kybology
 * 
 * ConfigurationHandler
 * 
 * @author PaleoCrafter, Weneg
 * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
 * 
 */
public class ConfigurationHandler {

	public static Configuration configuration;

	public static void init(File configFile) {

		configuration = new Configuration(configFile);

		try {
			configuration.load();

		} catch (Exception e) {
			FMLLog.log(Level.SEVERE, e, DRASH.MOD_NAME
					+ " has had a problem loading its configuration");
		} finally {
			configuration.save();
		}
	}

	public static void set(String categoryName, String propertyName,
			String newValue) {

		configuration.load();
		if (configuration.getCategoryNames().contains(categoryName)) {
			if (configuration.getCategory(categoryName).containsKey(
					propertyName)) {
				configuration.getCategory(categoryName).get(propertyName)
						.set(newValue);
			}
		}
		configuration.save();
	}

}
