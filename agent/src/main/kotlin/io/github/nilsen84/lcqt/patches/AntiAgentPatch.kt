package io.github.nilsen84.lcqt.patches

import io.github.nilsen84.lcqt.Patch
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.JumpInsnNode

class AntiAgentPatch : Patch() {
    override fun transform(node: ClassNode): Boolean {
        if (!node.name.startsWith("com/moonsworth/lunar")) return false
        for (method in node.methods) {
            // Check if the method is named "check"
            if (method.name.equals("check")) {
                // Iterate through each instruction in the method
                for (insn in method.instructions) {
                    // Check if the instruction is a jump instruction
                    if (insn is JumpInsnNode) {
                        // Check if the jump instruction is of type IFNE (if not equal to zero)
                        if (insn.opcode == Opcodes.IFNE) {
                            // Replace IFNE with IFEQ (if equal to zero)
                            insn.opcode = Opcodes.IFEQ
                            // Thus bypassing the Agent check.
                            return true
                        }
                    }
                }
            }
        }
        return false
    }
}