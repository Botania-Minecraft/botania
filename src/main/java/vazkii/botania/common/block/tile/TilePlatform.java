/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under a
 * Creative Commons Attribution-NonCommercial-ShareAlike 3.0 License
 * (http://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB)
 * 
 * File Created @ [Jun 7, 2014, 2:24:51 PM (GMT)]
 */
package vazkii.botania.common.block.tile;

import vazkii.botania.api.mana.IManaCollisionGhost;

public class TilePlatform extends TileCamo implements IManaCollisionGhost {

	@Override
	public boolean isGhost() {
		return true;
	}

}
