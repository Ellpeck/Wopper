package de.ellpeck.wopper;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.List;

public class TileEntityWopper extends TileEntity implements ITickable{

    private boolean didFirstTick;

    private IItemHandler handlerPull;
    private IItemHandler handlerPush;

    private boolean isEnabled;

    @Override
    public void update(){
        if(!this.world.isRemote){
            if(this.isEnabled && this.world.getTotalWorldTime()%Wopper.wopperSpeed == 0){
                if(this.handlerPush != null){
                    if(this.handlerPull != null){
                        extract:
                        for(int i = 0; i < this.handlerPull.getSlots(); i++){
                            ItemStack stack = this.handlerPull.extractItem(i, 1, true);

                            if(!stack.isEmpty()){
                                for(int j = 0; j < this.handlerPush.getSlots(); j++){
                                    ItemStack left = this.handlerPush.insertItem(j, stack, false);

                                    if(!ItemStack.areItemStacksEqual(stack, left)){
                                        int toExtract = left.isEmpty() ? stack.getCount() : stack.getCount()-left.getCount();
                                        this.handlerPull.extractItem(i, toExtract, false);

                                        break extract;
                                    }
                                }
                            }
                        }
                    }
                    else{
                        List<EntityItem> items = this.world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(this.pos.getX(), this.pos.getY()+0.5, this.pos.getZ(), this.pos.getX()+1, this.pos.getY()+2, this.pos.getZ()+1));
                        if(items != null && !items.isEmpty()){
                            for(EntityItem item : items){
                                if(item != null && !item.isDead){
                                    for(int i = 0; i < this.handlerPush.getSlots(); i++){
                                        ItemStack left = this.handlerPush.insertItem(i, item.getEntityItem(), false);

                                        if(!left.isEmpty()){
                                            item.setEntityItemStack(left);
                                        }
                                        else{
                                            item.setDead();
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if(!this.didFirstTick){
                this.onChange();
                this.didFirstTick = true;
            }
        }
    }

    public void onChange(){
        this.handlerPull = null;
        this.handlerPush = null;

        TileEntity from = this.world.getTileEntity(this.pos.up());
        if(from != null && from.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.DOWN)){
            this.handlerPull = from.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.DOWN);
        }

        IBlockState state = this.world.getBlockState(this.pos);
        EnumFacing facing = state.getValue(BlockWopper.FACING);
        BlockPos toPos = this.pos.offset(facing);

        if(this.world.isBlockLoaded(toPos)){
            TileEntity to = this.world.getTileEntity(toPos);
            if(to != null){
                EnumFacing opp = facing.getOpposite();
                if(to.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, opp)){
                    this.handlerPush = to.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, opp);
                }
            }
        }

        this.isEnabled = !Wopper.canBeDeactivated || this.world.isBlockIndirectlyGettingPowered(this.pos) <= 0;
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState){
        return !oldState.getBlock().isAssociatedBlock(newState.getBlock());
    }
}