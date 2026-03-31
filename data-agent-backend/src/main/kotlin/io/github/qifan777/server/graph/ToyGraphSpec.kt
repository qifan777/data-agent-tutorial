package io.github.qifan777.server.graph

object ToyGraphSpec {
    const val NAME = "toy-branch-streaming-graph"

    object Node {
        const val ROUTE = "ROUTE_NODE"
        const val CONFIRM = "CONFIRM_NODE"
        const val TRAVEL_PLAN = "TRAVEL_PLAN_NODE"
        const val STUDY_PLAN = "STUDY_PLAN_NODE"
        const val WRAP_UP = "WRAP_UP_NODE"
        const val INTERRUPT_NODE = CONFIRM

    }

    object StateKey {
        const val INPUT = "input"
        const val SCENE = "scene"
        const val SCENE_LABEL = "sceneLabel"
        const val DRAFT = "draft"
        const val FINAL_OUTPUT = "finalOutput"
        const val CONFIRMATION_APPROVED = "confirmationApproved"
        const val CONFIRMATION_FEEDBACK = "confirmationFeedback"
    }

    object Scene {
        const val TRAVEL = "travel"
        const val STUDY = "study"
    }


    object MessageMetadataKey {
        const val CONFIRMATION_APPROVED = "confirmationApproved"
        const val CONFIRMATION_FEEDBACK = "confirmationFeedback"
    }
}
