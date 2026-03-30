package io.github.qifan777.server.agent.prompt

import org.springframework.ai.chat.prompt.PromptTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.stereotype.Component

@Component
class PromptManager(
    @Value("classpath:/prompts/intent-recognition.st") private val intentRecognitionResource: Resource,
    @Value("classpath:/prompts/evidence-query-rewrite.st") private val evidenceQueryRewriteResource: Resource,
    @Value("classpath:/prompts/agent-knowledge.st") private val agentKnowledgeResource: Resource,
    @Value("classpath:/prompts/query-enhancement.st") private val queryEnhancementResource: Resource,
    @Value("classpath:/prompts/feasibility-assessment.st") private val feasibilityAssessmentResource: Resource,
    @Value("classpath:/prompts/mix-selector.st") private val mixSelectorResource: Resource,
    @Value("classpath:/prompts/semantic-consistency.st") private val semanticConsistencyResource: Resource,
    @Value("classpath:/prompts/new-sql-generate.st") private val newSqlGeneratorResource: Resource,
    @Value("classpath:/prompts/planner.st") private val plannerResource: Resource,
    @Value("classpath:/prompts/report-generator-plain.st") private val reportGeneratorPlainResource: Resource,
    @Value("classpath:/prompts/sql-error-fixer.st") private val sqlErrorFixerResource: Resource,
    @Value("classpath:/prompts/python-generator.st") private val pythonGeneratorResource: Resource,
    @Value("classpath:/prompts/python-analyze.st") private val pythonAnalyzeResource: Resource,
    @Value("classpath:/prompts/business-knowledge.st") private val businessKnowledgeResource: Resource,
    @Value("classpath:/prompts/semantic-model.st") private val semanticModelResource: Resource,
    @Value("classpath:/prompts/data-view-analyze.st") private val dataViewAnalyzeResource: Resource
) {
    val intentRecognitionPromptTemplate = PromptTemplate(intentRecognitionResource)
    val evidenceQueryRewritePromptTemplate = PromptTemplate(evidenceQueryRewriteResource)
    val agentKnowledgePromptTemplate = PromptTemplate(agentKnowledgeResource)
    val queryEnhancementPromptTemplate = PromptTemplate(queryEnhancementResource)
    val feasibilityAssessmentPromptTemplate = PromptTemplate(feasibilityAssessmentResource)
    val mixSelectorPromptTemplate = PromptTemplate(mixSelectorResource)
    val semanticConsistencyPromptTemplate = PromptTemplate(semanticConsistencyResource)
    val newSqlGeneratorPromptTemplate = PromptTemplate(newSqlGeneratorResource)
    val plannerPromptTemplate = PromptTemplate(plannerResource)
    val reportGeneratorPlainPromptTemplate = PromptTemplate(reportGeneratorPlainResource)
    val sqlErrorFixerPromptTemplate = PromptTemplate(sqlErrorFixerResource)
    val pythonGeneratorPromptTemplate = PromptTemplate(pythonGeneratorResource)
    val pythonAnalyzePromptTemplate = PromptTemplate(pythonAnalyzeResource)
    val businessKnowledgePromptTemplate = PromptTemplate(businessKnowledgeResource)
    val semanticModelPromptTemplate = PromptTemplate(semanticModelResource)
    val dataViewAnalyzePromptTemplate = PromptTemplate(dataViewAnalyzeResource)
}