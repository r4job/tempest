import pandas as pd
from traffic_patterns import *

id_to_pattern = {
    'A': LinearTrendPattern(base=200, slope=1.5),
    'B': DiurnalPattern(base=300, peak=500),
    'C': WeeklyPattern(weekday_base=250, weekend_boost=600),
    'D': FlashSpikePattern(spike_indices=[720, 1440], spike_value=1000),
    'E': HeavyTailPattern(base=100, scale=400),
    'F': NoisyPattern(base=200, noise_level=50),
    'G': RandomWalkPattern(start_value=250, scale=10),
    'H': FeedbackLoopPattern(base=150, feedback_rate=0.1),
    'I': CascadingFailurePattern(base=300, failure_start=1000, drop_amount=200),
    'J': QueueBuildupPattern(base=100, peak=700, rise_start=800, rise_end=1000)
}

def generate_timestamps(start_time: datetime, days: int, interval_minutes: int = 60):
    timestamps = []
    t = start_time
    while len(timestamps) < int((24 * 60 / interval_minutes) * days):
        timestamps.append(t)
        t += timedelta(minutes=interval_minutes)
    return timestamps

def simulate_traffic(id_to_pattern, start_time, days, output_format="csv", out_file="traffic.csv"):
    timestamps = generate_timestamps(start_time, days)
    all_data = []
    with open(out_file, "w") as f:
        if output_format == "csv":
            f.write("id,timestamp,value\n")
        for item_id, pattern in id_to_pattern.items():
            values = pattern.generate(timestamps)
            for ts, val in zip(timestamps, values):
                val = round(max(val, 0), 2)
                if output_format == "csv":
                    f.write(f"{item_id},{ts.isoformat()},{val}\n")
                elif output_format == "vw":
                    hour = ts.hour
                    minute = ts.minute
                    weekday = ts.weekday()
                    line = f"{val} |id_{item_id} hour:{hour} minute:{minute} weekday:{weekday}\n"
                    f.write(line)
                all_data.append((item_id, ts, val))
    return pd.DataFrame(all_data, columns=["id", "timestamp", "value"])

if __name__ == "__main__":
    start_time = datetime(2025, 3, 1)
    df = simulate_traffic(id_to_pattern, start_time, days=7, output_format="vw", out_file="traffic.vw")
    print("Simulation completed and saved to 'traffic.vw'")
