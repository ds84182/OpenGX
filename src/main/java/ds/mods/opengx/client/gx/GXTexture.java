package ds.mods.opengx.client.gx;

import java.awt.image.BufferedImage;
import java.io.IOException;

import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourceManager;

public class GXTexture extends AbstractTexture {
	private final int[] dynamicTextureData;
    /** width of this icon in pixels */
    public final int width;
    /** height of this icon in pixels */
    public final int height;

    public GXTexture(BufferedImage par1BufferedImage)
    {
        this(par1BufferedImage.getWidth(), par1BufferedImage.getHeight());
        par1BufferedImage.getRGB(0, 0, par1BufferedImage.getWidth(), par1BufferedImage.getHeight(), this.dynamicTextureData, 0, par1BufferedImage.getWidth());
        this.updateDynamicTexture();
    }
    
    public GXTexture(int par1, int par2, int[] data)
    {
    	this.width = par1;
    	this.height = par2;
    	this.dynamicTextureData = data;
    	TextureUtil.allocateTexture(this.getGlTextureId(), par1, par2);
    	this.updateDynamicTexture();
    }

    public GXTexture(int par1, int par2)
    {
        this.width = par1;
        this.height = par2;
        this.dynamicTextureData = new int[par1 * par2];
        TextureUtil.allocateTexture(this.getGlTextureId(), par1, par2);
    }

    public void loadTexture(IResourceManager par1ResourceManager) throws IOException {}

    public void updateDynamicTexture()
    {
        TextureUtil.uploadTexture(this.getGlTextureId(), this.dynamicTextureData, this.width, this.height);
    }

    public int[] getTextureData()
    {
        return this.dynamicTextureData;
    }

	@Override
	protected void finalize() throws Throwable {
		this.deleteGlTexture();
		super.finalize();
	}
}
