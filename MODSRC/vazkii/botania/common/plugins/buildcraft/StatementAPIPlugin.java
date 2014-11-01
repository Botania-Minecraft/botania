package vazkii.botania.common.plugins.buildcraft;

import java.util.ArrayList;
import java.util.Collection;

import vazkii.botania.api.mana.IManaBlock;
import vazkii.botania.api.mana.IManaReceiver;
import vazkii.botania.common.block.tile.TileRuneAltar;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.ITriggerExternal;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.api.statements.ITriggerProvider;
import buildcraft.api.statements.StatementManager;

public class StatementAPIPlugin implements ITriggerProvider {
	public static final ITriggerExternal triggerManaEmpty = new TriggerManaLevel(TriggerManaLevel.State.Empty);
	public static final ITriggerExternal triggerManaContains = new TriggerManaLevel(TriggerManaLevel.State.Contains);
	public static final ITriggerExternal triggerManaSpace = new TriggerManaLevel(TriggerManaLevel.State.Space);
	public static final ITriggerExternal triggerManaFull = new TriggerManaLevel(TriggerManaLevel.State.Full);
	public static final ITriggerInternal triggerManaDetector = new TriggerManaDetector();
	
	public static final ITriggerExternal triggerRuneAltarCanCraft = new TriggerRuneAltarCanCraft();

	public StatementAPIPlugin() {
		StatementManager.registerStatement(triggerManaEmpty);
		StatementManager.registerStatement(triggerManaContains);
		StatementManager.registerStatement(triggerManaSpace);
		StatementManager.registerStatement(triggerManaFull);
		StatementManager.registerStatement(triggerManaDetector);

		StatementManager.registerStatement(triggerRuneAltarCanCraft);
		
		StatementManager.registerTriggerProvider(this);
	}

	@Override
	public Collection<ITriggerInternal> getInternalTriggers(
			IStatementContainer container) {
		ArrayList<ITriggerInternal> list = new ArrayList<ITriggerInternal>();
		list.add(triggerManaDetector);
		return list;
	}

	@Override
	public Collection<ITriggerExternal> getExternalTriggers(
			ForgeDirection side, TileEntity tile) {
		ArrayList<ITriggerExternal> list = new ArrayList<ITriggerExternal>();
		
		if (tile instanceof IManaBlock) {
			list.add(triggerManaEmpty);
			list.add(triggerManaContains);
			
			if (tile instanceof IManaReceiver) {
				list.add(triggerManaSpace);
				list.add(triggerManaFull);
			}
		}
		
		if (tile instanceof TileRuneAltar) {
			list.add(triggerRuneAltarCanCraft);
		}
		
		return list;
	}
}
