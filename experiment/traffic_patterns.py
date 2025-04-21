from abc import ABC, abstractmethod
from datetime import datetime
import numpy as np

# --- Base class for all traffic patterns ---
class TrafficPattern(ABC):
    @abstractmethod
    def generate(self, timestamps: list[datetime]) -> list[float]:
        pass

# --- Concrete implementations of different traffic patterns ---

class LinearTrendPattern(TrafficPattern):
    def __init__(self, base=10, slope=0.5, noise=1):
        self.base = base
        self.slope = slope
        self.noise = noise

    def generate(self, timestamps):
        return [self.base + i * self.slope + np.random.normal(0, self.noise) for i in range(len(timestamps))]

class DiurnalPattern(TrafficPattern):
    def __init__(self, base=10, peak=30):
        self.base = base
        self.peak = peak

    def generate(self, timestamps):
        return [self.base + (self.peak if 8 <= ts.hour <= 20 else 0) + np.random.normal(0, 2) for ts in timestamps]

class WeeklyPattern(TrafficPattern):
    def __init__(self, weekday_base=15, weekend_boost=25):
        self.weekday_base = weekday_base
        self.weekend_boost = weekend_boost

    def generate(self, timestamps):
        return [self.weekend_boost if ts.weekday() >= 5 else self.weekday_base + np.random.normal(0, 1) for ts in timestamps]

class FlashSpikePattern(TrafficPattern):
    def __init__(self, spike_indices=None, spike_value=100, base=10):
        self.spike_indices = spike_indices or [50]
        self.spike_value = spike_value
        self.base = base

    def generate(self, timestamps):
        return [self.spike_value if i in self.spike_indices else self.base + np.random.normal(0, 2) for i in range(len(timestamps))]

class HeavyTailPattern(TrafficPattern):
    def __init__(self, base=5, scale=20):
        self.base = base
        self.scale = scale

    def generate(self, timestamps):
        return [self.base + np.random.pareto(2) * self.scale for _ in timestamps]

class NoisyPattern(TrafficPattern):
    def __init__(self, base=10, noise_level=5):
        self.base = base
        self.noise_level = noise_level

    def generate(self, timestamps):
        return [self.base + np.random.normal(0, self.noise_level) for _ in timestamps]

class RandomWalkPattern(TrafficPattern):
    def __init__(self, start_value=10, scale=2):
        self.start_value = start_value
        self.scale = scale

    def generate(self, timestamps):
        walk = [self.start_value]
        for _ in range(1, len(timestamps)):
            walk.append(max(walk[-1] + np.random.normal(0, self.scale), 0))
        return walk

class FeedbackLoopPattern(TrafficPattern):
    def __init__(self, base=10, feedback_rate=0.05):
        self.base = base
        self.feedback_rate = feedback_rate

    def generate(self, timestamps):
        values = []
        val = self.base
        for _ in timestamps:
            val += self.feedback_rate * val + np.random.normal(0, 2)
            values.append(min(max(val, 0), 1_000_000))
        return values

class CascadingFailurePattern(TrafficPattern):
    def __init__(self, base=10, failure_start=70, drop_amount=30):
        self.base = base
        self.failure_start = failure_start
        self.drop_amount = drop_amount

    def generate(self, timestamps):
        return [self.base - self.drop_amount if i >= self.failure_start else self.base + np.random.normal(0, 1)
                for i in range(len(timestamps))]

class QueueBuildupPattern(TrafficPattern):
    def __init__(self, base=10, peak=50, rise_start=40, rise_end=60):
        self.base = base
        self.peak = peak
        self.rise_start = rise_start
        self.rise_end = rise_end

    def generate(self, timestamps):
        series = []
        for i in range(len(timestamps)):
            if i < self.rise_start:
                series.append(self.base)
            elif i <= self.rise_end:
                scale = (i - self.rise_start) / (self.rise_end - self.rise_start)
                series.append(self.base + scale * (self.peak - self.base) + np.random.normal(0, 2))
            else:
                series.append(self.base + np.random.normal(0, 2))
        return series
