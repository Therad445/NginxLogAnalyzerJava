package backend.academy.loganalyzer.template;

import org.junit.jupiter.api.Test;

import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class LogResultTest {

    @Test
    void LogResultTest_IsValid() {
        //Arrange
        long totalRequests = 100;
        double averageResponseSize = 512.5;
        Map<String, Long> resourceCounts = Map.of("/index.html", 50L, "/about.html", 25L);
        Map<Integer, Long> statusCodeCounts = Map.of(200, 80L, 404, 20L);
        double percentile = 201.5;
        //Act
        LogResult logResult = new LogResult
            (totalRequests, averageResponseSize, resourceCounts, statusCodeCounts, percentile);
        //Assert
        assertEquals(totalRequests, logResult.totalRequests());
        assertEquals(averageResponseSize, logResult.averageResponseSize());
        assertEquals(resourceCounts, logResult.resourceCounts());
        assertEquals(statusCodeCounts, logResult.statusCodeCounts());
        assertEquals(percentile, logResult.percentile());
    }

    @Test
    void totalRequests_IsNegative() {
        //Arrange
        long totalRequests = -100;
        double averageResponseSize = 512.5;
        Map<String, Long> resourceCounts = Map.of("/index.html", 50L, "/about.html", 25L);
        Map<Integer, Long> statusCodeCounts = Map.of(200, 80L, 404, 20L);
        double percentile = 201.5;

        //Act
        Exception exception = assertThrows(IllegalArgumentException.class, () -> new LogResult
            (totalRequests, averageResponseSize, resourceCounts, statusCodeCounts, percentile));
        //Assert
        assertEquals("totalRequests меньше нуля", exception.getMessage());
    }

    @Test
    void averageResponseSize_IsNegative() {
        //Arrange
        long totalRequests = 100;
        double averageResponseSize = -512.5;
        Map<String, Long> resourceCounts = Map.of("/index.html", 50L, "/about.html", 25L);
        Map<Integer, Long> statusCodeCounts = Map.of(200, 80L, 404, 20L);
        double percentile = 201.5;

        //Act
        Exception exception = assertThrows(IllegalArgumentException.class, () -> new LogResult
            (totalRequests, averageResponseSize, resourceCounts, statusCodeCounts, percentile));
        //Assert
        assertEquals("averageResponseSize меньше нуля", exception.getMessage());
    }

    @Test
    void resourceCounts_IsNull() {
        //Arrange
        long totalRequests = 100;
        double averageResponseSize = 512.5;
        Map<String, Long> resourceCounts = null;
        Map<Integer, Long> statusCodeCounts = Map.of(200, 80L, 404, 20L);
        double percentile = 201.5;

        //Act
        Exception exception = assertThrows(NullPointerException.class, () -> new LogResult
            (totalRequests, averageResponseSize, resourceCounts, statusCodeCounts, percentile));
        //Assert
        assertEquals("resourceCounts пустой", exception.getMessage());
    }

    @Test
    void statusCodeCounts_IsNull() {
        //Arrange
        long totalRequests = 100;
        double averageResponseSize = 512.5;
        Map<String, Long> resourceCounts = Map.of("/index.html", 50L, "/about.html", 25L);
        Map<Integer, Long> statusCodeCounts = null;
        double percentile = 201.5;

        //Act
        Exception exception = assertThrows(NullPointerException.class, () -> new LogResult
            (totalRequests, averageResponseSize, resourceCounts, statusCodeCounts, percentile));
        //Assert
        assertEquals("statusCodeCounts пустой", exception.getMessage());
    }

    @Test
    void percentile_IsNegative() {
        //Arrange
        long totalRequests = 100;
        double averageResponseSize = 512.5;
        Map<String, Long> resourceCounts = Map.of("/index.html", 50L, "/about.html", 25L);
        Map<Integer, Long> statusCodeCounts = Map.of(200, 80L, 404, 20L);
        double percentile = -201.5;

        //Act
        Exception exception = assertThrows(IllegalArgumentException.class, () -> new LogResult
            (totalRequests, averageResponseSize, resourceCounts, statusCodeCounts, percentile));
        //Assert
        assertEquals("percentile меньше нуля", exception.getMessage());
    }
}
