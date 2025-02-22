package kr.alpha93.craftdsl.entrypoint

import kr.alpha93.dokdo.Holder
import kr.alpha93.ph.commands.Command
import kr.alpha93.ph.commands.Executor
import kr.alpha93.ph.commands.Requires
import kr.alpha93.ph.commands.SubCommand
import kr.alpha93.ph.paper.commands.OperatorRequirement
import org.bukkit.command.CommandSender

@Command("dsl")
@Requires(handle = OperatorRequirement::class)
internal object DSLCommand {

    @JvmStatic
    internal var loader by Holder<PaperPlugin>()

    @SubCommand("reload")
    private object ReloadCommand {

        @Executor
        private fun executor(sender: CommandSender) {
            loader.loadAsynchronously()
        }

    }

}
