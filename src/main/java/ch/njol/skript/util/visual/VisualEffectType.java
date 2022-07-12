/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.util.visual;

import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.Noun;
import ch.njol.skript.log.BlockingLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.variables.Variables;
import ch.njol.yggdrasil.Fields;
import ch.njol.yggdrasil.YggdrasilSerializable;
import ch.njol.yggdrasil.YggdrasilSerializer;
import org.bukkit.Effect;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.eclipse.jdt.annotation.Nullable;

import java.io.NotSerializableException;
import java.io.StreamCorruptedException;
import java.util.Objects;
import java.util.function.BiFunction;

public class VisualEffectType implements YggdrasilSerializable {

	static {
		Variables.yggdrasil.registerClassResolver(new YggdrasilSerializer<VisualEffectType>() {
			@Nullable
			@Override
			public Class<? extends VisualEffectType> getClass(String id) {
				return id.equals("VisualEffectType") ? VisualEffectType.class : null;
			}

			@Override
			public Fields serialize(VisualEffectType o) {
				Fields f = new Fields();
				f.putObject("id", o.getId());
				return f;
			}

			@Nullable
			@Override
			public <E extends VisualEffectType> E newInstance(Class<E> c) {
				return null;
			}

			@Override
			public boolean canBeInstantiated(Class<? extends VisualEffectType> c) {
				return false;
			}

			@Override
			public void deserialize(VisualEffectType o, Fields fields) { }

			@SuppressWarnings("ConstantConditions")
			@Override
			public <E extends VisualEffectType> E deserialize(Class<E> c, Fields fields) throws StreamCorruptedException, NotSerializableException {
				return (E) VisualEffects.get(fields.getObject("id", String.class));
			}

			@Nullable
			@Override
			public String getID(Class<?> c) {
				return c.equals(VisualEffectType.class) ? "VisualEffectType" : null;
			}
		});
	}

	private static final String LANGUAGE_NODE = "visual effects";

	private final Enum<?> effect;

	private String pattern;
	private Noun name;

	private boolean colorable = false;
	private BiFunction<Object[], Location, Object> dataSupplier = (o, location) -> null;

	private VisualEffectType(Enum<?> effect) {
		this.effect = effect;
	}

	public void setColorable() {
		colorable = true;
	}

	public boolean isColorable() {
		return colorable;
	}

	public void withData(BiFunction<Object[], Location, Object> dataSupplier) {
		this.dataSupplier = dataSupplier;
	}

	@Nullable
	public Object getData(Object[] raw, Location location) {
		return dataSupplier.apply(raw, location);
	}

	public String getId() {
		return effect.getDeclaringClass().getSimpleName() + "." + effect.name();
	}

	public Noun getName() {
		return name;
	}

	public String getPattern() {
		return pattern;
	}

	public boolean isEffect() {
		return effect instanceof Effect;
	}

	public boolean isEntityEffect() {
		return effect instanceof EntityEffect;
	}

	public boolean isParticle() {
		return effect instanceof Particle;
	}

	public Effect getEffect() {
		if (!isEffect())
			throw new IllegalStateException();
		return (Effect) effect;
	}

	public EntityEffect getEntityEffect() {
		if (!isEntityEffect())
			throw new IllegalStateException();
		return (EntityEffect) effect;
	}

	public Particle getParticle() {
		if (!isParticle())
			throw new IllegalStateException();
		return (Particle) effect;
	}

	@Nullable
	static VisualEffectType of(Enum<?> effect) {
		Objects.requireNonNull(effect);

		VisualEffectType type = new VisualEffectType(effect);
		String node = LANGUAGE_NODE + "." + type.getId();

		if (!Language.keyExistsDefault(node + ".pattern"))
			return null;

		String pattern = Language.get(node + ".pattern");

		type.name = new Noun(node + ".name");

		String areaPattern = Language.get_(LANGUAGE_NODE + ".area_expression");
		type.pattern = pattern + " " + (areaPattern != null ? areaPattern : "");

		return type;
	}

}
