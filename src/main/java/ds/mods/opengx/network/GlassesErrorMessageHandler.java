package ds.mods.opengx.network;

import net.minecraft.client.Minecraft;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ds.mods.opengx.Glasses;
import ds.mods.opengx.OpenGX;
import ds.mods.opengx.gx.IGX.RunnableRender;

public class GlassesErrorMessageHandler implements IMessageHandler<GlassesErrorMessage, IMessage> {

	@Override
	@SideOnly(Side.CLIENT)
	public IMessage onMessage(final GlassesErrorMessage message, MessageContext ctx) {
		//we do
		World w = OpenGX.proxy.getClientWorld();
		Glasses g = Glasses.get(message.uuid, w);
		if (g == null)
		{
			return null;
		}
		g.gx.update();
		System.out.println(message.error);
		g.gx.gx.setRenderRedirect(new RunnableRender(){

			public void run(int fbwidth, int fbheight) {
				//render the error onto screen
				//display error
				//fill render with fbwidth and fbheight
				GL11.glClearColor (1.0f, 0.0f, 0.0f, 0.5f);//transparentui
				GL11.glClear (GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT); // Clear Screen And Depth Buffer on the fbo to red
				String textFirst = "Error:";
				int th = Minecraft.getMinecraft().fontRenderer.listFormattedStringToWidth(textFirst, fbwidth).size();
				Minecraft.getMinecraft().fontRenderer.drawSplitString(textFirst, 0, 0, fbwidth, 0xFFFFFFFF);
				Minecraft.getMinecraft().fontRenderer.drawSplitString(message.error, 0, th*8+2, fbwidth, 0xFFFFFFFF);
			}

		});
		g.gx.gx.requestRerender();
		return null;
	}

}
