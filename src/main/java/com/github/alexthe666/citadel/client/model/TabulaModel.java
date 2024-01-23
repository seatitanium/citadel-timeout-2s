package com.github.alexthe666.citadel.client.model;

import com.github.alexthe666.citadel.client.model.container.TabulaCubeContainer;
import com.github.alexthe666.citadel.client.model.container.TabulaCubeGroupContainer;
import com.github.alexthe666.citadel.client.model.container.TabulaModelContainer;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TabulaModel extends AdvancedEntityModel {
   protected Map<String, AdvancedModelBox> cubes = new HashMap<>();
   protected List<AdvancedModelBox> rootBoxes = new ArrayList();
   protected ITabulaModelAnimator tabulaAnimator;
   public ModelAnimator llibAnimator;
   protected Map<String, AdvancedModelBox> identifierMap = new HashMap<>();
   protected double[] scale;

   public TabulaModel(TabulaModelContainer container, ITabulaModelAnimator tabulaAnimator) {
      this.textureWidth = container.getTextureWidth();
      this.textureHeight = container.getTextureHeight();
      this.tabulaAnimator = tabulaAnimator;

      for(TabulaCubeContainer cube : container.getCubes()) {
         this.parseCube(cube, null);
      }

      container.getCubeGroups().forEach(this::parseCubeGroup);
      this.updateDefaultPose();
      this.scale = container.getScale();
      this.llibAnimator = ModelAnimator.create();
   }

   public TabulaModel(TabulaModelContainer container) {
      this(container, null);
   }

   private void parseCubeGroup(TabulaCubeGroupContainer container) {
      for(TabulaCubeContainer cube : container.getCubes()) {
         this.parseCube(cube, null);
      }

      container.getCubeGroups().forEach(this::parseCubeGroup);
   }

   private void parseCube(TabulaCubeContainer cube, AdvancedModelBox parent) {
      AdvancedModelBox box = this.createBox(cube);
      this.cubes.put(cube.getName(), box);
      this.identifierMap.put(cube.getIdentifier(), box);
      if (parent != null) {
         parent.addChild(box);
      } else {
         this.rootBoxes.add(box);
      }

      for(TabulaCubeContainer child : cube.getChildren()) {
         this.parseCube(child, box);
      }
   }

   private AdvancedModelBox createBox(TabulaCubeContainer cube) {
      int[] textureOffset = cube.getTextureOffset();
      double[] position = cube.getPosition();
      double[] rotation = cube.getRotation();
      double[] offset = cube.getOffset();
      int[] dimensions = cube.getDimensions();
      float scaleIn = 0.0F;
      AdvancedModelBox box = new AdvancedModelBox(this, cube.getName());
      box.setTextureOffset(textureOffset[0], textureOffset[1]);
      box.mirror = cube.isTextureMirrorEnabled();
      box.setRotationPoint((float)position[0], (float)position[1], (float)position[2]);
      box.addBox((float)offset[0], (float)offset[1], (float)offset[2], (float)dimensions[0], (float)dimensions[1], (float)dimensions[2], scaleIn);
      box.rotateAngleX = (float)Math.toRadians(rotation[0]);
      box.rotateAngleY = (float)Math.toRadians(rotation[1]);
      box.rotateAngleZ = (float)Math.toRadians(rotation[2]);
      return box;
   }

   public void setRotationAngles(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float rotationYaw, float rotationPitch) {
      if (this.tabulaAnimator != null) {
         this.tabulaAnimator.setRotationAngles(this, entity, limbSwing, limbSwingAmount, ageInTicks, rotationYaw, rotationPitch, 1.0F);
      }
   }

   public AdvancedModelBox getCube(String name) {
      return (AdvancedModelBox)this.cubes.get(name);
   }

   public AdvancedModelBox getCubeByIdentifier(String identifier) {
      return (AdvancedModelBox)this.identifierMap.get(identifier);
   }

   public Map<String, AdvancedModelBox> getCubes() {
      return this.cubes;
   }

   public Iterable<ModelRenderer> getParts() {
      return ImmutableList.copyOf(this.rootBoxes);
   }

   @Override
   public Iterable<AdvancedModelBox> getAllParts() {
      return ImmutableList.copyOf(this.cubes.values());
   }
}