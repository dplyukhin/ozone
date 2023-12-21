import pandas as pd
import matplotlib.pyplot as plt

def plot_histogram_from_csv(file_paths):
    fig, axes = plt.subplots(nrows=2, ncols=2, figsize=(12, 8))
    axes = axes.flatten()

    for i, file_path in enumerate(file_paths):
        # Reading data from CSV file
        data = pd.read_csv(file_path, header=None)
        # Ignore the first 100 entries
        data = data.iloc[100:]

        # Print the min, max, and average values - along with the file_path
        print(f'{file_path} - min: {data[0].min()}, max: {data[0].max()}, avg: {data[0].mean()}')
        
        # Plotting histogram
        axes[i].hist(data[0], bins=50, color='blue', alpha=0.7)
        axes[i].set_title(f'Histogram of {file_path}')
        axes[i].set_xlabel('Latency (ms)')
        axes[i].set_ylabel('Frequency')
        axes[i].set_xlim(left=0, right=30)

    plt.tight_layout()
    plt.show()

file_paths = [
    'data/concurrentproducers/worker1-latencies.csv', 
    'data/concurrentproducers/worker2-latencies.csv', 
    'data/inorderproducers/worker1-latencies.csv', 
    'data/inorderproducers/worker2-latencies.csv']

plot_histogram_from_csv(file_paths)
