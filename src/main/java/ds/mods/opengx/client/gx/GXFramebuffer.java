package ds.mods.opengx.client.gx;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureUtil;

import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;

import ds.mods.opengx.client.RenderUtils;

public class GXFramebuffer {
	public final int fbo;
	public final int tex;
	public int width, height;

	public GXFramebuffer(int w, int h)
	{
		IntBuffer buffer = ByteBuffer.allocateDirect(1*4).order(ByteOrder.nativeOrder()).asIntBuffer();
		EXTFramebufferObject.glGenFramebuffersEXT( buffer ); // generate
		fbo = buffer.get();
		tex = TextureUtil.glGenTextures();
		// initialize texture
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, tex);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, w, h, 0, GL11.GL_RGBA, GL11.GL_INT, (java.nio.ByteBuffer) null);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		width = w;
		height = h;
		
		EXTFramebufferObject.glBindFramebufferEXT( EXTFramebufferObject.GL_FRAMEBUFFER_EXT, fbo);
		EXTFramebufferObject.glFramebufferTexture2DEXT( EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_COLOR_ATTACHMENT0_EXT, GL11.GL_TEXTURE_2D, tex, 0);
		
		GL11.glPushAttrib(GL11.GL_VIEWPORT_BIT);
		GL11.glViewport(0, 0, width, height);
		GL11.glClearColor(0, 0, 0, 255);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glPopAttrib();
		
		int result = EXTFramebufferObject.glCheckFramebufferStatusEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT);
		if (result!=EXTFramebufferObject.GL_FRAMEBUFFER_COMPLETE_EXT) {
			EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, 0);
			EXTFramebufferObject.glDeleteFramebuffersEXT(fbo);
			throw new RuntimeException("exception "+result+" when checking FBO status");
		}
		Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(true);
	}

	public void bind()
	{
		EXTFramebufferObject.glBindFramebufferEXT( EXTFramebufferObject.GL_FRAMEBUFFER_EXT, fbo);
		GL11.glPushAttrib(GL11.GL_VIEWPORT_BIT);
		GL11.glViewport( 0, 0, width, height);
		GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
		GL11.glOrtho(0, width, height, 0, -9999, 9999);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glPushMatrix();
		GL11.glLoadIdentity();
		
		bindTexture();
		RenderUtils.texturedRectangle(0, 0, width, height, 0F, 1F, 1F, 0F); //render ourself onto fb
		unbindTexture();
	}
	
	public void unbind()
	{
		GL11.glPopMatrix();
		EXTFramebufferObject.glBindFramebufferEXT( EXTFramebufferObject.GL_FRAMEBUFFER_EXT, 0);
		GL11.glPopAttrib();
		Minecraft.getMinecraft().entityRenderer.setupOverlayRendering();
		Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(true);
	}
	
	public void bindTexture()
	{
		GL11.glPushAttrib(GL11.GL_TEXTURE_BIT);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, tex);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
	}
	
	public void unbindTexture()
	{
		GL11.glPopAttrib();
	}
	
	public void resize(int w, int h)
	{
		width = w;
		height = h;
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, tex);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, w, h, 0, GL11.GL_RGBA, GL11.GL_INT, (java.nio.ByteBuffer) null);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
	}

	@Override
	protected void finalize() throws Throwable {
		EXTFramebufferObject.glDeleteFramebuffersEXT(fbo);
		GL11.glDeleteTextures(tex);
		super.finalize();
	}
}
