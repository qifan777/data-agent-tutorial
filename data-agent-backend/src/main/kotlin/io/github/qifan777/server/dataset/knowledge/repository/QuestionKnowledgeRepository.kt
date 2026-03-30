package io.github.qifan777.server.dataset.knowledge.repository

import io.github.qifan777.server.dataset.knowledge.domain.QuestionKnowledge
import org.babyfish.jimmer.spring.repository.KRepository
import java.util.*

interface QuestionKnowledgeRepository : KRepository<QuestionKnowledge, UUID>{

}