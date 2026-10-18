package vn.edu.webpro.robotlab.business;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import vn.edu.webpro.robotlab.util.AssemblyPlacementJson;

class AssemblyReviewViewerTest {
    private AssemblyLayout layout() {
        AssemblyLayout layout = new AssemblyLayout();
        layout.setSessionId(42);
        layout.setStatus("COMPLETED");
        layout.getSpec().setId(8);
        layout.getSpec().setVersionNo(1);
        AssemblySlot slot = new AssemblySlot();
        slot.setId(10);
        slot.setComponentId("wheel");
        slot.setRequired(true);
        slot.getOrientations().add("Y180");
        layout.getSpec().getSlots().add(slot);
        AssemblyInstance item = new AssemblyInstance();
        item.setId(12);
        item.setComponentId("wheel");
        item.setLabel("Bánh </div><script> & \" riêng");
        layout.getInstances().add(item);
        AssemblyPlacement placed = new AssemblyPlacement();
        placed.setInstanceId(12);
        placed.setSlotId(10);
        placed.setOrientationCode("Y180");
        layout.getPlacements().add(placed);
        return layout;
    }

    private AssemblyReview review() {
        AssemblyReview review = new AssemblyReview();
        review.getSession().setId(42);
        review.getSession().setSpecId(8);
        review.getSession().setAssemblyMode("DETAILED");
        review.getSession().setStatus("COMPLETED");
        return review;
    }

    @Test void pinnedOldVersionRemainsDisplayableWithoutChangingAnySavedFacts() {
        AssemblyReview review = review();
        AssemblyLayout layout = layout();
        String before = AssemblyPlacementJson.layout(layout);
        assertTrue(review.canDisplayLayout(layout));
        assertEquals(before, AssemblyPlacementJson.layout(layout));
        layout.getSpec().setVersionNo(2);
        assertTrue(review.canDisplayLayout(layout));
        layout.getSpec().setId(9);
        assertFalse(review.canDisplayLayout(layout));
        layout.getSpec().setId(8);
        layout.setSessionId(43);
        assertFalse(review.canDisplayLayout(layout));
    }

    @Test void groupIncompleteAndMissingLayoutsNeverMasqueradeAsFinalModels() {
        AssemblyReview review = review();
        assertFalse(review.canDisplayLayout(null));
        review.getSession().setStatus("IN_PROGRESS");
        assertFalse(review.canDisplayLayout(layout()));
        review.getSession().setStatus("COMPLETED");
        review.getSession().setAssemblyMode("GROUP");
        assertFalse(review.canDisplayLayout(layout()));
        AssemblyLayout layout = layout();
        layout.getPlacements().clear();
        assertFalse(review().canDisplayLayout(layout));
        layout.setSpec(null);
        assertFalse(review().canDisplayLayout(layout));
    }

    @Test void brokenReferencesDuplicateInstancesAndNonFinitePosesAreNotDisplayed() {
        AssemblyLayout layout = layout();
        layout.getPlacements().get(0).setSlotId(999);
        assertFalse(review().canDisplayLayout(layout));
        layout = layout();
        layout.getPlacements().get(0).setOrientationCode("Y45");
        assertFalse(review().canDisplayLayout(layout));
        layout = layout();
        layout.getSpec().getSlots().get(0).setX(Double.NaN);
        assertFalse(review().canDisplayLayout(layout));
        layout = layout();
        layout.getPlacements().add(layout.getPlacements().get(0));
        assertFalse(review().canDisplayLayout(layout));
    }

    @Test void reviewPayloadKeepsPinnedIdentityAndQuotesLabelsWithoutEditMetadata() {
        String json = AssemblyPlacementJson.reviewLayout(layout());
        assertTrue(json.contains("\"sessionId\":\"42\""));
        assertTrue(json.contains("\"spec\":{\"id\":\"8\",\"versionNo\":1"));
        assertTrue(json.contains("\"instanceId\":\"12\",\"slotId\":\"10\",\"orientation\":\"Y180\""));
        assertTrue(json.contains("Bánh </div><script> & \\\" riêng"));
        assertFalse(json.contains("canEdit"));
        assertFalse(json.contains("report"));
        String api = AssemblyPlacementJson.layout(layout());
        assertTrue(api.contains("\"canEdit\":false,\"unchanged\":false,\"transitioned\":false"));
        assertTrue(api.contains("\"report\":"));
    }
}
