package de.mineformers.drash;

import java.io.File;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkMod;
import de.mineformers.drash.configuration.ConfigurationHandler;
import de.mineformers.drash.core.proxy.CommonProxy;
import de.mineformers.drash.lib.Version;
import de.mineformers.drash.network.ModPackets;
import de.mineformers.drash.network.PacketHandler;

/**
 * 
 * PowerGrid
 * 
 * PowerGrid
 * 
 * @author PaleoCrafter
 * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
 * 
 */
@Mod(modid = DRASH.MOD_ID,
		name = DRASH.MOD_NAME,
		version = Version.VERSION,
		dependencies = DRASH.DEPENDENCIES,
		certificateFingerprint = DRASH.FINGERPRINT)
@NetworkMod(channels = { DRASH.CHANNEL_NAME },
		clientSideRequired = true,
		serverSideRequired = false,
		packetHandler = PacketHandler.class)
public class DRASH {

	public static final String MOD_ID = "DRASH";
	public static final String MOD_NAME = "DRASH";
	public static final String CHANNEL_NAME = MOD_ID;
	public static final String FINGERPRINT = "";
	public static final String RESOURCE_PATH = "core" + File.separator;
	public static final String DEPENDENCIES = "required-after:Forge@[9.11.1.964,);after:IC2";
	public static final String SERVER_PROXY_CLASS = "de.mineformers.drash.core.proxy.CommonProxy";
	public static final String CLIENT_PROXY_CLASS = "de.mineformers.drash.core.proxy.ClientProxy";

	@Instance(DRASH.MOD_ID)
	public static DRASH instance;

	@SidedProxy(clientSide = DRASH.CLIENT_PROXY_CLASS,
			serverSide = DRASH.SERVER_PROXY_CLASS)
	public static CommonProxy proxy;

	@EventHandler
	public void serverStarting(FMLServerStartingEvent event) {
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		ModPackets.init();
		ConfigurationHandler.init(new File(event.getModConfigurationDirectory()
				.getAbsolutePath()
				+ File.separator
				+ DRASH.CHANNEL_NAME
				+ File.separator + DRASH.MOD_ID + ".cfg"));
	}

	@EventHandler
	public void load(FMLInitializationEvent event) {
		proxy.registerRendering();
		proxy.registerEventHandlers();
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
	}
}
