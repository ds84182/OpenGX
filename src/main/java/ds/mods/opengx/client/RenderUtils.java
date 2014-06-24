package ds.mods.opengx.client;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;

import org.lwjgl.opengl.ARBMultitexture;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GLContext;

public class RenderUtils {
	public static void setColor(int r, int g, int b)
	{
		GL11.glColor3f(r/255F, g/255F, b/255F);
	}

	public static void setColor(int r, int g, int b, int a)
	{
		GL11.glColor4f(r/255F, g/255F, b/255F, a/255F);
	}

	public static void rectangle(float x, float y, float w, float h)
	{
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glVertex2f(x, y);
		GL11.glVertex2f(x, y+h);
		GL11.glVertex2f(x+w, y+h);
		GL11.glVertex2f(x+w, y);
		GL11.glEnd();
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}

	public static void texturedRectangle(float x, float y, float w, float h, float u1, float v1, float u2, float v2)
	{
		texturedRectangle(x,y,w,h,u1,v1,u2,v2,0xFFFFFFFF);
	}

	public static void texturedRectangle(float x, float y, float w, float h, float u1, float v1, float u2, float v2, int color)
	{
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_COLOR_MATERIAL);
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.setColorOpaque_I(color);
		tessellator.addVertexWithUV(x, y, 0.0D, u1, v1);
		tessellator.addVertexWithUV(x, y+h, 0.0D, u1, v2);
		tessellator.addVertexWithUV(x+w, y+h, 0.0D, u2, v2);
		tessellator.addVertexWithUV(x+w, y, 0.0D, u2, v1);
		tessellator.draw();
	}

	public static void  disableLighting() {
		GL11.glDisable(GL11.GL_LIGHTING);
		if (GLContext.getCapabilities().GL_ARB_multitexture && !GLContext.getCapabilities().OpenGL13) {
			ARBMultitexture.glActiveTextureARB(OpenGlHelper.lightmapTexUnit);
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			ARBMultitexture.glActiveTextureARB(OpenGlHelper.defaultTexUnit);
		}
		else {
			GL13.glActiveTexture(OpenGlHelper.lightmapTexUnit);
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL13.glActiveTexture(OpenGlHelper.defaultTexUnit);
		}
	}

	public static void  enableLighting() {
		GL11.glEnable(GL11.GL_LIGHTING);
		if (GLContext.getCapabilities().GL_ARB_multitexture && !GLContext.getCapabilities().OpenGL13) {
			ARBMultitexture.glActiveTextureARB(OpenGlHelper.lightmapTexUnit);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			ARBMultitexture.glActiveTextureARB(OpenGlHelper.defaultTexUnit);
		}
		else {
			GL13.glActiveTexture(OpenGlHelper.lightmapTexUnit);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL13.glActiveTexture(OpenGlHelper.defaultTexUnit);
		}
	}
}
