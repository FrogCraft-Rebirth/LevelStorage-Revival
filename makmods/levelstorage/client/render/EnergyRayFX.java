package makmods.levelstorage.client.render;

import java.util.List;
import java.util.Random;

import org.lwjgl.opengl.GL11;

import makmods.levelstorage.logic.LSDamageSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class EnergyRayFX extends Particle {
	// public int particle = 16;

	private double offset = 0.0D;

	private double tX = 0.0D;
	private double tY = 0.0D;
	private double tZ = 0.0D;
	private double ptX = 0.0D;
	private double ptY = 0.0D;
	private double ptZ = 0.0D;

	private float length = 0.0F;
	private float rotYaw = 0.0F;
	private float rotPitch = 0.0F;
	private float prevYaw = 0.0F;
	private float prevPitch = 0.0F;
	private Entity targetEntity = null;

	private int type = 0;

	private float endMod = 1.0F;

	private boolean reverse = false;

	private boolean pulse = true;

	private int rotationspeed = 5;

	private float prevSize = 0.0F;
	public int impact;

	public EnergyRayFX(World par1World, double px, double py, double pz,
			double tx, double ty, double tz, float red, float green,
			float blue, int age) {
		super(par1World, px, py, pz, 0.0D, 0.0D, 0.0D);

		this.particleRed = red;
		this.particleGreen = green;
		this.particleBlue = blue;
		setSize(0.02F, 0.02F);
		//this.noClip = true; Not sure if it's correct replacement
		this.canCollide = false;
		this.motionX = 0.0D;
		this.motionY = 0.0D;
		this.motionZ = 0.0D;
		this.tX = tx;
		this.tY = ty;
		this.tZ = tz;
		this.prevYaw = this.rotYaw;
		this.prevPitch = this.rotPitch;
		this.particleMaxAge = age;
		Entity renderentity = Minecraft.getMinecraft().getRenderViewEntity();
		int visibleDistance = 1000;
		//if (!ModLoader.getMinecraftInstance().gameSettings.fancyGraphics)
		//	visibleDistance = 25;
		if (renderentity.getDistance(this.posX, this.posY, this.posZ) > visibleDistance)
			this.particleMaxAge = 0;
	}

	public void updateBeam(double x, double y, double z) {
		this.tX = x;
		this.tY = y;
		this.tZ = z;
		while (this.particleMaxAge - this.particleAge < 4)
			this.particleMaxAge += 1;
	}

	public void onUpdate() {
		this.prevPosX = this.posX;
		this.prevPosY = (this.posY + this.offset);
		this.prevPosZ = this.posZ;

		this.ptX = this.tX;
		this.ptY = this.tY;
		this.ptZ = this.tZ;

		this.prevYaw = this.rotYaw;
		this.prevPitch = this.rotPitch;

		float xd = (float) (this.posX - this.tX);
		float yd = (float) (this.posY - this.tY);
		float zd = (float) (this.posZ - this.tZ);
		this.length = MathHelper.sqrt_float(xd * xd + yd * yd + zd * zd);
		double var7 = MathHelper.sqrt_double(xd * xd + zd * zd);
		this.rotYaw = ((float) (Math.atan2(xd, zd) * 180.0D / 3.141592653589793D));
		this.rotPitch = ((float) (Math.atan2(yd, var7) * 180.0D / 3.141592653589793D));
		this.prevYaw = this.rotYaw;
		this.prevPitch = this.rotPitch;

		if (this.impact > 0)
			this.impact -= 1;

		List<EntityMob> mobs = this.worldObj.getEntitiesWithinAABB(EntityMob.class,
				new AxisAlignedBB(tX - 4, tY - 4, tZ - 4, tX + 4,
						tY + 4, tZ + 4));

		
		mobs.forEach(mob -> mob.attackEntityFrom(LSDamageSource.teslaRay, 10));

		if (this.particleAge++ >= this.particleMaxAge) {
			this.setExpired(); //Per Entity#setDead()
		}
	}

	public void setRGB(float r, float g, float b) {
		this.particleRed = r;
		this.particleGreen = g;
		this.particleBlue = b;
	}

	public void setType(int type) {
		this.type = type;
	}

	public void setEndMod(float endMod) {
		this.endMod = endMod;
	}

	public void setReverse(boolean reverse) {
		this.reverse = reverse;
	}

	public void setPulse(boolean pulse) {
		this.pulse = pulse;
	}

	public void setRotationspeed(int rotationspeed) {
		this.rotationspeed = rotationspeed;
	}

	Random rng = new Random();

	public void renderParticle(Tessellator tessellator, float f, float f1,
			float f2, float f3, float f4, float f5) {
		tessellator.draw();
		GL11.glPushMatrix();
		GL11.glPushMatrix();
		//GL11.glTranslated(Math.random() * 10.0F, Math.random() * 10.0F,
		//		Math.random() * 10.0F);
		float var9 = 1.0F;
		float slide = (float) this.worldObj.getWorldTime();
		float rot = (float) (this.worldObj.provider.getWorldTime()
				% (360 / this.rotationspeed) * this.rotationspeed)
				+ this.rotationspeed * f;

		float size = 1.0F;
		if (this.pulse) {
			size = Math.min(this.particleAge / 4.0F, 1.0F);
			size = (float) (this.prevSize + (size - this.prevSize) * f);
		}

		float op = 0.4F;
		if ((this.pulse) && (this.particleMaxAge - this.particleAge <= 4)) {
			op = 0.4F - (4 - (this.particleMaxAge - this.particleAge)) * 0.1F;
		}
		switch (this.type) {
		default:
			//RenderHelper.bindTexture(ClientProxy.TESLA_RAY_1);
			break;
		}

		GL11.glTexParameterf(3553, 10242, 10497.0F);
		GL11.glTexParameterf(3553, 10243, 10497.0F);

		GL11.glDisable(2884);

		float var11 = slide + f;
		if (this.reverse)
			var11 *= -1.0F;
		float var12 = -var11 * 0.2F - MathHelper.floor_float(-var11 * 0.1F);

		GL11.glEnable(3042);
		GL11.glBlendFunc(770, 1);
		GL11.glDepthMask(false);

		float xx = (float) (this.prevPosX + (this.posX - this.prevPosX) * f - Particle.interpPosX);
		float yy = (float) (this.prevPosY + (this.posY - this.prevPosY) * f - Particle.interpPosY);
		float zz = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * f - Particle.interpPosZ);
		GL11.glTranslated(xx, yy, zz);

		float ry = (float) (this.prevYaw + (this.rotYaw - this.prevYaw) * f);
		float rp = (float) (this.prevPitch + (this.rotPitch - this.prevPitch)
				* f);
		GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
		GL11.glRotatef(180.0F + ry, 0.0F, 0.0F, -1.0F);
		GL11.glRotatef(rp, 1.0F, 0.0F, 0.0F);

		double var44 = -0.15D * size;
		double var17 = 0.15D * size;
		double var44b = -0.15D * size * this.endMod;
		double var17b = 0.15D * size * this.endMod;

		GL11.glRotatef(rot, 0.0F, 1.0F, 0.0F);
		for (int t = 0; t < 3; t++) {
			double var29 = this.length * size * var9;
			double var31 = 0.0D;
			double var33 = 1.0D;
			double var35 = -1.0F + var12 + t / 3.0F;
			double var37 = this.length * size * var9 + var35;

			GL11.glRotatef(60.0F, 0.0F, 1.0F, 0.0F);
			tessellator.getBuffer().begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
			//tessellator.setBrightness(200);
			tessellator.getBuffer().color(this.particleRed, this.particleGreen, this.particleBlue, op);
			tessellator.getBuffer().pos(var44b, var29, 0.0D).tex(var33, var37);
			tessellator.getBuffer().pos(var44, 0.0D, 0.0D).tex(var33, var35);
			tessellator.getBuffer().pos(var17, 0.0D, 0.0D).tex(var31, var35);
			tessellator.getBuffer().pos(var17b, var29, 0.0D).tex(var31, var37);
			tessellator.draw();
		}

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glDepthMask(true);
		GL11.glDisable(3042);
		GL11.glEnable(2884);
		GL11.glPopMatrix();

		if (this.impact > 0)
			renderImpact(tessellator, f, f1, f2, f3, f4, f5);

		// RenderHelper.bindTexture(ClientProxy.TESLA_PARTICLES_TEXTURE);
		tessellator.getBuffer().begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
		this.prevSize = size;
		GL11.glDisable(2884);
		GL11.glPopMatrix();
	}

	public void renderImpact(Tessellator tessellator, float f, float f1,
			float f2, float f3, float f4, float f5) {
		GL11.glPushMatrix();
		GL11.glDepthMask(false);
		GL11.glEnable(3042);
		GL11.glBlendFunc(770, 1);

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.66F);
		int part = this.particleAge % 16;

		float var8 = part % 8 / 8.0F;
		float var9 = var8 + 0.125F;
		float var10 = 0.375F + part / 8 / 8.0F;
		float var11 = var10 + 0.125F;
		float var12 = this.endMod / 2.0F / (6 - this.impact);

		float var13 = (float) (this.ptX + (this.tX - this.ptX) * f - Particle.interpPosX);
		float var14 = (float) (this.ptY + (this.tY - this.ptY) * f - Particle.interpPosY);
		float var15 = (float) (this.ptZ + (this.tZ - this.ptZ) * f - Particle.interpPosZ);
		float var16 = 1.0F;

		tessellator.getBuffer().begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
		//tessellator.setBrightness(200);
		tessellator.getBuffer().color(this.particleRed, this.particleGreen, this.particleBlue, 0.66F);
		tessellator.getBuffer().pos(var13 - f1 * var12 - f4 * var12, var14 - f2
				* var12, var15 - f3 * var12 - f5 * var12).tex(var9, var11);
		tessellator.getBuffer().pos(var13 - f1 * var12 + f4 * var12, var14 + f2
				* var12, var15 - f3 * var12 + f5 * var12).tex(var9, var10);
		tessellator.getBuffer().pos(var13 + f1 * var12 + f4 * var12, var14 + f2
				* var12, var15 + f3 * var12 + f5 * var12).tex(var8, var10);
		tessellator.getBuffer().pos(var13 + f1 * var12 - f4 * var12, var14 - f2
				* var12, var15 + f3 * var12 - f5 * var12).tex(var8, var11);

		tessellator.draw();

		GL11.glDisable(3042);
		GL11.glDepthMask(true);
		GL11.glPopMatrix();
	}
}
