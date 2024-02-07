package io.github.nilsen84.lcqt.patches

import io.github.nilsen84.lcqt.LcqtPatcher.config
import io.github.nilsen84.lcqt.Patch
import io.github.nilsen84.lcqt.util.internalNameOf
import net.weavemc.api.bytecode.asm
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*
import java.lang.invoke.StringConcatFactory

// https://discord.com/channels/1081741604899000470/1157740336878198976/1204427156814565417
class CustomMetadataPatch : Patch() {
    override fun transform(node: ClassNode): Boolean {
        if (!node.name.startsWith("com/moonsworth/lunar/client/")) {
            return false
        }

        val clinit = node.methods.find("<clinit>") ?: return false
        with(clinit.instructions) {
            val invokeDynamicInsnNode = filterIsInstance<InvokeDynamicInsnNode>()
                .find {
                    it.name == "makeConcatWithConstants"
                            && it.desc == "(Ljava/lang/String;)Ljava/lang/String;"
                            && it.bsm.owner == internalNameOf<StringConcatFactory>()
                            && it.bsmArgs.contentEquals(arrayOf("https://api.\u0001"))
                } ?: return false
            println("[METADATA] Found Class ${node.name}!")

            insert(invokeDynamicInsnNode, asm {
                ldc(config.customMetadataURL)
            })

            remove(invokeDynamicInsnNode.previous)
            remove(invokeDynamicInsnNode)
        }
        return true
    }
}

private fun MutableList<MethodNode>.find(name: String): MethodNode? {
    return this.find { method ->
        method as MethodNode
        method.name == name
    }
}

inline fun <reified T : AbstractInsnNode> AbstractInsnNode.next(p: (T) -> Boolean = { true }): T? {
    return generateSequence(next) { it.next }.filterIsInstance<T>().find(p)
}
