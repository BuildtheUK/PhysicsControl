package org.btuk.pcontrol.inventory;

import org.btuk.pcontrol.data.category.PControlCategory;

import javax.annotation.Nonnull;
import java.util.*;

public class TriggersSlots {
    private static final int INVENTORY_WIDTH = 9; // columns amount
    private static final int INVENTORY_HEIGHT = 3; // rows amount
    private static final char TRIGGER_SLOT = '*';
    private static final char EMPTY_SLOT = '-';

    private static final Map<Integer, List<Short>> slotsByTriggersAmount = new HashMap<>();

    static {
        reg("", // 1
            "---------",
            "----*----",
            "---------"
        );
        reg("", // 2
            "---------",
            "---*-*---",
            "---------"
        );
        reg("", // 3
            "---------",
            "---***---",
            "---------"
        );
        reg("", // 4
            "----*----",
            "---***---",
            "---------"
        );
        reg("", // 5
            "---------",
            "--*****--",
            "---------"
        );
        reg("", // 6
            "---***---",
            "---***---",
            "---------"
        );
        reg("", // 7
            "---------",
            "-*******-",
            "---------"
        );
        reg("", // 8
            "---***---",
            "--*****--",
            "---------"
        );
        reg("", // 9
            "---***---",
            "---***---",
            "---***---"
        );
        reg("", // 10
            "--*****--",
            "--*****--",
            "---------"
        );
        reg("", // 11
            "---***---",
            "--*****--",
            "---***---"
        );
        reg("", // 12
            "--*****--",
            "-*******-",
            "---------"
        );
        reg("", // 13
            "--*****--",
            "--*****--",
            "---***---"
        );
        reg("", // 14
            "-*******-",
            "-*******-",
            "---------"
        );
        reg("", // 15
            "--*****--",
            "--*****--",
            "--*****--"
        );
        reg("", // 15
            "-*******-",
            "*********",
            "---------"
        );
        reg("", // 17
            "--*****--",
            "-*******-",
            "--*****--"
        );
        reg("", // 18
            "*********",
            "*********",
            "---------"
        );
        reg("", // 19
            "-*******-",
            "-*******-",
            "--*****--"
        );
        reg("", // 20
            "-*******-",
            "-***-***-",
            "-*******-"
        );
        reg("", // 21
            "-*******-",
            "-*******-",
            "-*******-"
        );
        reg("", // 22
            "-*******-",
            "****-****",
            "-*******-"
        );
        reg("", // 23
            "-*******-",
            "*********",
            "-*******-"
        );
        reg("", // 24
            "*********",
            "****-****",
            "-*******-"
        );
        reg("", // 25
            "*********",
            "*********",
            "-*******-"
        );
        reg("", // 26
            "*********",
            "****-****",
            "*********"
        );
        reg("", // 27
            "*********",
            "*********",
            "*********"
        );
    }

    private static void reg(@Nonnull String... matrix) {
        char[] chars;
        int triggersAmount = 0;
        List<Short> slots = new ArrayList<>();
        if (matrix.length - 1 != INVENTORY_HEIGHT) throw new IllegalArgumentException("Wrong rows amount");
        matrix = Arrays.copyOfRange(matrix, 1, matrix.length);
        for (int rowIndex = 0; rowIndex < matrix.length; rowIndex++) {
            chars = matrix[rowIndex].toCharArray();
            if (chars.length != INVENTORY_WIDTH) throw new IllegalArgumentException("Wrong columns amount");
            for (int columnIndex = 0; columnIndex < chars.length; columnIndex++) {
                if (chars[columnIndex] == TRIGGER_SLOT) {
                    triggersAmount++;
                    slots.add((short) (rowIndex * 9 + columnIndex));
                } else if (chars[columnIndex] != EMPTY_SLOT) {
                    throw new IllegalArgumentException("Unknown char at indexes [" + rowIndex + "," + columnIndex + "]");
                }
            }
        }
        if (slotsByTriggersAmount.put(triggersAmount, slots) != null) {
            throw new IllegalArgumentException("Duplicate slots for " + triggersAmount + " triggers");
        }
    }

    @Nonnull
    public static List<Short> getSlots(@Nonnull PControlCategory category) {
        int triggersAmount = category.getTriggers().size();
        List<Short> result = slotsByTriggersAmount.get(triggersAmount);
        if (result == null) {
            throw new IllegalStateException(
                "No slots found for " + triggersAmount + " triggers (category " + category + ")");
        }
        if (triggersAmount != result.size()) {
            throw new IllegalStateException(
                "Wrong slots amount (" + result.size() + ") " +
                    "for " + triggersAmount + " triggers (category " + category + ")");
        }
        return result;
    }
}
