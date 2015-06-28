package StevenDimDoors.mod_pocketDimClient;

import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.particle.EntityFireworkSparkFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import StevenDimDoors.mod_pocketDim.core.PocketManager;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GoggleRiftFX extends EntityFireworkSparkFX
{
    private int field_92049_a = 160;
    private boolean field_92054_ax;
    private boolean field_92048_ay;
    private final EffectRenderer field_92047_az;
    private float field_92050_aA;
    private float field_92051_aB;
    private float field_92052_aC;
    private boolean field_92053_aD;

    public GoggleRiftFX(World par1World, double par2, double par4, double par6, double par8, double par10, double par12, EffectRenderer par14EffectRenderer)
    {

        super(par1World, par2, par4, par6, par12, par12, par12, par14EffectRenderer);
        this.motionX = par8;
        this.motionY = par10;
        this.motionZ = par12;
        this.field_92047_az = par14EffectRenderer;
        this.particleScale *= 0.75F;
        this.particleMaxAge = 40 + this.rand.nextInt(26);
        this.noClip = true;
    }

    public void func_92045_e(boolean par1)
    {
        this.field_92054_ax = par1;
    }

    public void func_92043_f(boolean par1)
    {
        this.field_92048_ay = par1;
    }

    public void func_92044_a(int par1)
    {
        float var2 = ((par1 & 16711680) >> 16) / 255.0F;
        float var3 = ((par1 & 65280) >> 8) / 255.0F;
        float var4 = ((par1 & 255) >> 0) / 255.0F;
        float var5 = 1.0F;
        this.setRBGColorF(var2 * var5, var3 * var5, var4 * var5);
    }

    public void func_92046_g(int par1)
    {
        this.field_92050_aA = ((par1 & 16711680) >> 16) / 255.0F;
        this.field_92051_aB = ((par1 & 65280) >> 8) / 255.0F;
        this.field_92052_aC = ((par1 & 255) >> 0) / 255.0F;
        this.field_92053_aD = true;
    }

    /**
     * returns the bounding box for this entity
     */
    @Override
    public AxisAlignedBB getBoundingBox()
    {
        return null;
    }

    /**
     * Returns true if this entity should push and be pushed by other entities when colliding.
     */
    @Override
    public boolean canBePushed()
    {
        return false;
    }

    @Override
    public void renderParticle(Tessellator par1Tessellator, float par2, float par3, float par4, float par5, float par6, float par7)
    {
        if (!this.field_92048_ay || this.particleAge < this.particleMaxAge / 3 || (this.particleAge + this.particleMaxAge) / 3 % 2 == 0)
        {


            this.doRenderParticle(par1Tessellator, par2, par3, par4, par5, par6, par7);
        }
    }

    public void doRenderParticle(Tessellator par1Tessellator, float par2, float par3, float par4, float par5, float par6, float par7)
    {
        float f6 = this.particleTextureIndexX / 16.0F;
        float f7 = f6 + 0.0624375F;
        float f8 = this.particleTextureIndexY / 16.0F;
        float f9 = f8 + 0.0624375F;
        float f10 = 0.1F * this.particleScale;

        if (this.particleIcon != null)
        {
            f6 = this.particleIcon.getMinU();
            f7 = this.particleIcon.getMaxU();
            f8 = this.particleIcon.getMinV();
            f9 = this.particleIcon.getMaxV();
        }

        float f11 = (float)(this.prevPosX + (this.posX - this.prevPosX) * par2 - interpPosX);
        float f12 = (float)(this.prevPosY + (this.posY - this.prevPosY) * par2 - interpPosY);
        float f13 = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * par2 - interpPosZ);
        float f14 = 0F;

        if (PocketManager.createDimensionData(worldObj).isPocketDimension())
        {
            f14 = 0.7F;
        }

        par1Tessellator.setColorRGBA_F(this.particleRed * f14, this.particleGreen * f14, this.particleBlue * f14, (float) .7);
        par1Tessellator.addVertexWithUV(f11 - par3 * f10 - par6 * f10, f12 - par4 * f10, f13 - par5 * f10 - par7 * f10, f7, f9);
        par1Tessellator.addVertexWithUV(f11 - par3 * f10 + par6 * f10, f12 + par4 * f10, f13 - par5 * f10 + par7 * f10, f7, f8);
        par1Tessellator.addVertexWithUV(f11 + par3 * f10 + par6 * f10, f12 + par4 * f10, f13 + par5 * f10 + par7 * f10, f6, f8);
        par1Tessellator.addVertexWithUV(f11 + par3 * f10 - par6 * f10, f12 - par4 * f10, f13 + par5 * f10 - par7 * f10, f6, f9);
    }

    /**
     * Called to update the entity's position/logic.
     */
    @Override
    public void onUpdate()
    {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        if (this.particleAge++ >= this.particleMaxAge)
        {
            this.setDead();
        }

        if (this.particleAge > this.particleMaxAge / 2)
        {
            this.setAlphaF(1.0F - ((float)this.particleAge - (float)(this.particleMaxAge / 2)) / this.particleMaxAge);

            if (this.field_92053_aD)
            {
                this.particleRed += (this.field_92050_aA - this.particleRed) * 0.2F;
                this.particleGreen += (this.field_92051_aB - this.particleGreen) * 0.2F;
                this.particleBlue += (this.field_92052_aC - this.particleBlue) * 0.2F;
            }
        }

        this.setParticleTextureIndex(this.field_92049_a + (7 - this.particleAge * 8 / this.particleMaxAge));
        // this.motionY -= 0.004D;
        this.moveEntity(this.motionX, this.motionY, this.motionZ);
        this.motionX *= 0.9100000262260437D;
        this.motionY *= 0.9100000262260437D;
        this.motionZ *= 0.9100000262260437D;

        if (this.onGround)
        {
            this.motionX *= 0.699999988079071D;
            this.motionZ *= 0.699999988079071D;
        }

        if (this.field_92054_ax && this.particleAge < this.particleMaxAge / 2 && (this.particleAge + this.particleMaxAge) % 2 == 0)
        {
            GoggleRiftFX var1 = new GoggleRiftFX(this.worldObj, this.posX, this.posY, this.posZ, 0.0D, 0.0D, 0.0D, this.field_92047_az);
            var1.setRBGColorF(this.particleRed, this.particleGreen, this.particleBlue);
            var1.particleAge = var1.particleMaxAge / 2;

            if (this.field_92053_aD)
            {
                var1.field_92053_aD = true;
                var1.field_92050_aA = this.field_92050_aA;
                var1.field_92051_aB = this.field_92051_aB;
                var1.field_92052_aC = this.field_92052_aC;
            }

            var1.field_92048_ay = this.field_92048_ay;
            this.field_92047_az.addEffect(var1);
        }
    }

    @Override
    public int getBrightnessForRender(float par1)
    {
        return 15728880;
    }

    /**
     * Gets how bright this entity is.
     */
    @Override
    public float getBrightness(float par1)
    {
        return 1.0F;
    }
}
