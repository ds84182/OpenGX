package ds.mods.opengx.client;

import java.util.EnumMap;
import java.util.EnumSet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import ds.mods.opengx.Glasses;
import ds.mods.opengx.OpenGX;
import ds.mods.opengx.items.ItemGlasses;
import ds.mods.opengx.network.GlassesButtonEventMessage;
import ds.mods.opengx.network.GlassesButtonEventMessage.Button;

public class Keybindings {

	public static EnumSet<Button> buttonSet = EnumSet.noneOf(Button.class);
	public static EnumMap<Button,KeyBinding> buttonMap = new EnumMap<Button, KeyBinding>(Button.class);
	public static EnumMap<Button,Long> buttonPressStart = new EnumMap<Button, Long>(Button.class);
	
	public static KeyBinding onoff;
	public static KeyBinding screenpower;
	public static KeyBinding actionkey1;
	public static KeyBinding actionkey2;
	public static KeyBinding actionkeymod;

	public static void init() {

		//to turn the glasses on and off
		onoff = new KeyBinding("key.onoff", Keyboard.KEY_O, "key.categories.opengx");

		//to turn the screen on and off
		screenpower = new KeyBinding("key.screenpower", Keyboard.KEY_P, "key.categories.opengx");

		//action keys
		actionkey1 = new KeyBinding("key.actionkey1", Keyboard.KEY_LBRACKET, "key.categories.opengx");
		actionkey2 = new KeyBinding("key.actionkey2", Keyboard.KEY_RBRACKET, "key.categories.opengx");
		actionkeymod = new KeyBinding("key.actionkeymod", Keyboard.KEY_RSHIFT, "key.categories.opengx");

		// Register both KeyBindings to the ClientRegistry
		ClientRegistry.registerKeyBinding(onoff);
		ClientRegistry.registerKeyBinding(screenpower);
		ClientRegistry.registerKeyBinding(actionkey1);
		ClientRegistry.registerKeyBinding(actionkey2);
		ClientRegistry.registerKeyBinding(actionkeymod);
		
		buttonMap.put(Button.ONOFF, onoff);
		buttonMap.put(Button.SCREENPOWER, screenpower);
		buttonMap.put(Button.ACTION1, actionkey1);
		buttonMap.put(Button.ACTION2, actionkey2);
		buttonMap.put(Button.ACTIONM, actionkeymod);
	}

}
