package com.github.alexthe666.citadel;

import com.github.alexthe666.citadel.animation.IAnimatedEntity;
import com.github.alexthe666.citadel.client.CitadelItemRenderProperties;
import com.github.alexthe666.citadel.client.CitadelPatreonRenderer;
import com.github.alexthe666.citadel.client.event.EventGetOutlineColor;
import com.github.alexthe666.citadel.client.event.EventPosePlayerHand;
import com.github.alexthe666.citadel.client.gui.GuiCitadelBook;
import com.github.alexthe666.citadel.client.gui.GuiCitadelPatreonConfig;
import com.github.alexthe666.citadel.client.model.TabulaModel;
import com.github.alexthe666.citadel.client.model.TabulaModelHandler;
import com.github.alexthe666.citadel.client.patreon.SpaceStationPatreonRenderer;
import com.github.alexthe666.citadel.server.entity.CitadelEntityData;
import com.mojang.blaze3d.vertex.PoseStack;
import java.io.IOException;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.SkinCustomizationScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent.Post;
import net.minecraftforge.client.event.ScreenEvent.DrawScreenEvent;
import net.minecraftforge.client.event.ScreenEvent.InitScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(
   bus = Bus.MOD,
   value = {Dist.CLIENT}
)
public class ClientProxy extends ServerProxy {
   public static TabulaModel CITADEL_MODEL;
   private static final ResourceLocation CITADEL_TEXTURE = new ResourceLocation("citadel", "textures/patreon/citadel_model.png");
   private static final ResourceLocation CITADEL_TEXTURE_RED = new ResourceLocation("citadel", "textures/patreon/citadel_model_red.png");
   private static final ResourceLocation CITADEL_TEXTURE_GRAY = new ResourceLocation("citadel", "textures/patreon/citadel_model_gray.png");
   private static final String RICKROLL_URL = "http://techslides.com/demos/sample-videos/small.mp4";
   private static final ResourceLocation RICKROLL_LOCATION = new ResourceLocation("citadel:rickroll");

   @Override
   public void onPreInit() {
      try {
         CITADEL_MODEL = new TabulaModel(TabulaModelHandler.INSTANCE.loadTabulaModel("/assets/citadel/models/citadel_model"));
      } catch (IOException var2) {
         var2.printStackTrace();
      }

      CitadelPatreonRenderer.register("citadel", new SpaceStationPatreonRenderer(CITADEL_TEXTURE));
      CitadelPatreonRenderer.register("citadel_red", new SpaceStationPatreonRenderer(CITADEL_TEXTURE_RED));
      CitadelPatreonRenderer.register("citadel_gray", new SpaceStationPatreonRenderer(CITADEL_TEXTURE_GRAY));
   }

   @SubscribeEvent
   public void openScreen(InitScreenEvent event) {
      if (event.getScreen() instanceof SkinCustomizationScreen && Minecraft.getInstance().player != null) {
         try {
            String username = Minecraft.getInstance().player.getName().getContents();
            if (Citadel.PATREONS.contains(username)) {
               event.addListener(
                  new Button(
                     event.getScreen().width / 2 - 100,
                     event.getScreen().height / 6 + 150,
                     200,
                     20,
                     new TranslatableComponent("citadel.gui.patreon_rewards_option").withStyle(ChatFormatting.GREEN),
                     p_213080_2_ -> Minecraft.getInstance().setScreen(new GuiCitadelPatreonConfig(event.getScreen(), Minecraft.getInstance().options))
                  )
               );
            }
         } catch (Exception var3) {
            var3.printStackTrace();
         }
      }
   }

   @SubscribeEvent
   public void drawScreen(DrawScreenEvent event) {
   }

   @SubscribeEvent
   public void playerRender(Post event) {
      PoseStack matrixStackIn = event.getPoseStack();
      String username = event.getPlayer().getName().getContents();
      if (event.getPlayer().isModelPartShown(PlayerModelPart.CAPE)) {
         if (Citadel.PATREONS.contains(username)) {
            CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(Minecraft.getInstance().player);
            String rendererName = tag.contains("CitadelFollowerType") ? tag.getString("CitadelFollowerType") : "citadel";
            if (!rendererName.equals("none")) {
               CitadelPatreonRenderer renderer = CitadelPatreonRenderer.get(rendererName);
               if (renderer != null) {
                  float distance = tag.contains("CitadelRotateDistance") ? tag.getFloat("CitadelRotateDistance") : 2.0F;
                  float speed = tag.contains("CitadelRotateSpeed") ? tag.getFloat("CitadelRotateSpeed") : 1.0F;
                  float height = tag.contains("CitadelRotateHeight") ? tag.getFloat("CitadelRotateHeight") : 1.0F;
                  renderer.render(
                     matrixStackIn,
                     event.getMultiBufferSource(),
                     event.getPackedLight(),
                     event.getPartialTick(),
                     event.getEntityLiving(),
                     distance,
                     speed,
                     height
                  );
               }
            }
         }
      }
   }

   @Override
   public void handleAnimationPacket(int entityId, int index) {
      Player player = Minecraft.getInstance().player;
      if (player != null) {
         IAnimatedEntity entity = (IAnimatedEntity)player.level.getEntity(entityId);
         if (entity != null) {
            if (index == -1) {
               entity.setAnimation(IAnimatedEntity.NO_ANIMATION);
            } else {
               entity.setAnimation(entity.getAnimations()[index]);
            }

            entity.setAnimationTick(0);
         }
      }
   }

   @Override
   public void handlePropertiesPacket(String propertyID, CompoundTag compound, int entityID) {
      if (compound != null) {
         Player player = Minecraft.getInstance().player;
         Entity entity = player.level.getEntity(entityID);
         if ((propertyID.equals("CitadelPatreonConfig") || propertyID.equals("CitadelTagUpdate")) && entity instanceof LivingEntity) {
            CitadelEntityData.setCitadelTag((LivingEntity)entity, compound);
         }
      }
   }

   @Override
   public Object getISTERProperties() {
      return new CitadelItemRenderProperties();
   }

   @Override
   public void openBookGUI(ItemStack book) {
      Minecraft.getInstance().setScreen(new GuiCitadelBook(book));
   }

   @SubscribeEvent
   public void outlineColorTest(EventGetOutlineColor event) {
   }

   @SubscribeEvent
   public void animateHandTest(EventPosePlayerHand event) {
   }
}