package it.fantasytoolkit.buffdebuffgenerator.result;

import it.fantasytoolkitcore.core.pojo.GeneratedElementResult;

import java.util.List;

public record BuffDebuffResult(List<BuffElement> buffs, List<DebuffElement> debuffs) implements GeneratedElementResult {
}
