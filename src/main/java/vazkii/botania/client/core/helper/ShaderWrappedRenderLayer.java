/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.core.helper;

import net.minecraft.client.render.RenderLayer;

import vazkii.botania.client.lib.LibResources;

import javax.annotation.Nullable;

import java.util.Objects;
import java.util.Optional;

public class ShaderWrappedRenderLayer extends RenderLayer {
	private final RenderLayer delegate;
	private final ShaderHelper.BotaniaShader shader;

	@Nullable
	private final ShaderCallback cb;

	public ShaderWrappedRenderLayer(ShaderHelper.BotaniaShader shader, @Nullable ShaderCallback cb, RenderLayer delegate) {
		super(LibResources.PREFIX_MOD + delegate.toString() + "_with_" + shader.name(), delegate.getVertexFormat(), delegate.getDrawMode(), delegate.getExpectedBufferSize(), delegate.hasCrumbling(), true,
				() -> {
					delegate.startDrawing();
					ShaderHelper.useShader(shader, cb);
				},
				() -> {
					ShaderHelper.releaseShader();
					delegate.endDrawing();
				});
		this.delegate = delegate;
		this.shader = shader;
		this.cb = cb;
	}

	@Override
	public Optional<RenderLayer> getAffectedOutline() {
		return delegate.getAffectedOutline();
	}

	@Override
	public boolean equals(@Nullable Object other) {
		return other instanceof ShaderWrappedRenderLayer
				&& delegate.equals(((ShaderWrappedRenderLayer) other).delegate)
				&& shader == ((ShaderWrappedRenderLayer) other).shader
				&& Objects.equals(cb, ((ShaderWrappedRenderLayer) other).cb);
	}

	@Override
	public int hashCode() {
		return Objects.hash(delegate, shader, cb);
	}
}
