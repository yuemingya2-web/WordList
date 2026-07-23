package com.vocabmaster.app.domain

/**
 * 内置职场英语核心词表（仅单词本身）。
 * 首次启动时由 WordRepository 通过 Dictionary API 拉取释义/音标/例句后缓存到本地。
 *
 * 选取标准：高频出现于邮件、会议、汇报、谈判等职场沟通场景。
 */
object VocabSeeder {

    val words: List<String> = listOf(
        // 会议与协作
        "agenda", "minutes", "recap", "deadline", "schedule", "postpone", "reschedule",
        "delegate", "collaborate", "coordinate", "follow", "update", "progress", "milestone",
        "deliverable", "outcome", "feedback", "revise", "amend", "draft", "finalize",

        // 项目与执行
        "project", "proposal", "implement", "deploy", "integrate", "optimize", "assign",
        "prioritize", "escalate", "resolve", "troubleshoot", "assess", "evaluate", "estimate",
        "forecast", "analyze", "streamline", "facilitate", "execute", "launch",

        // 商务与财务
        "negotiate", "contract", "client", "budget", "revenue", "profit", "invoice",
        "compensation", "reimburse", "quota", "target", "stakeholder", "vendor", "procurement",

        // 沟通与汇报
        "presentation", "brief", "clarify", "confirm", "acknowledge", "address", "emphasize",
        "recommend", "propose", "advise", "inform", "notify", "circulate", "summarize",
        "highlight", "reiterate", "convey", "articulate", "correspondence",

        // 职场状态
        "onboard", "recruit", "candidate", "appraisal", "performance", "tenure", "resign",
        "transition", "handover", "leave", "absence", "substitute",

        // 邮件与文档
        "attachment", "signature", "regards", "sincerely", "subject", "recipient",
        "forward", "reply", "acknowledge", "pending", "overdue", "urgent"
    )
}
