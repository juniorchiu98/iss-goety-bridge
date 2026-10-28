package com.jj321.issgoety.main;

import com.jj321.issgoety.IssGoety;
import com.jj321.issgoety.focus.FocusSpell;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModSpells {
    public static final DeferredRegister<AbstractSpell> SPELLS =
            DeferredRegister.create(SpellRegistry.SPELL_REGISTRY_KEY, IssGoety.MOD_ID);

    /** One shared spell entry; its display and behavior resolve the focus on the currently held item. */
    public static final RegistryObject<AbstractSpell> FOCUS_CAST =
            SPELLS.register("focus_cast", FocusSpell::new);
}
