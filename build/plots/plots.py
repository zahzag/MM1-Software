#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Tue Feb 25 17:42:14 2025

@author: ayman
"""
#############average#############"
#importations
import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
from itertools import combinations

"""//////////////////////////////////////////////"""
#load excel file
data = pd.read_excel("workbook.xlsx")
data = data.drop(columns=['Mes. Power','Avg. Freq'])

data= data.select_dtypes(include=['number'])
type(data)

### COMPUTE mean of tests

data_avg=data.groupby(['Mes frequency','Mod. Lambda']).mean().reset_index()
len(data_avg.groupby('Mes frequency')) # check how many freq we have 
data_avg.to_excel("workbook_avg.xlsx") # save data 
"""//////////////////////////////////////////////"""


#load excel file
data_numeric = pd.read_excel("workbook_avg.xlsx",index_col=0)

"""filtre data to modeled and mesured """
filter_modled=[col for col in data_numeric if col.startswith("Mod")]
filter_measured=[col for col in data_numeric if col.startswith("Mes")]
modled_data=data_numeric[filter_modled]
measured_data=data_numeric[filter_measured]



"""#### 2D Pais #####"""

"""modled combinaisons"""
columns_pairs=list(combinations(modled_data,2))
# Set up the figure for multiple subplots
num_plots = len(columns_pairs)
cols = 3  # Number of columns in subplot grid
rows = -(-num_plots // cols)  # Calculate rows needed

fig, axes = plt.subplots(rows, cols, figsize=(15, rows * 5))
axes = axes.flatten()  # Flatten in case of 2D axes

# Loop through each column pair and create a scatter plot
for i, (col1, col2) in enumerate(columns_pairs):
    sns.scatterplot(x=data_numeric[col1], y=data_numeric[col2], ax=axes[i])
    axes[i].set_title(f"{col1} vs {col2}")

"""measured combinaisons"""
columns_pairs=list(combinations(measured_data,2))
# Set up the figure for multiple subplots
num_plots = len(columns_pairs)
cols = 3  # Number of columns in subplot grid
rows = -(-num_plots // cols)  # Calculate rows needed

fig, axes = plt.subplots(rows, cols, figsize=(15, rows * 5))
axes = axes.flatten()  # Flatten in case of 2D axes

# Loop through each column pair and create a scatter plot
for i, (col1, col2) in enumerate(columns_pairs):
    sns.scatterplot(x=data_numeric[col1], y=data_numeric[col2], ax=axes[i])
    axes[i].set_title(f"{col1} vs {col2}")
"""all combinaisons"""
# all posible combinaisons 
columns_pairs = list(combinations(data_numeric.columns,2))

# Set up the figure for multiple subplots
num_plots = len(columns_pairs)
cols = 3  # Number of columns in subplot grid
rows = -(-num_plots // cols)  # Calculate rows needed

fig, axes = plt.subplots(rows, cols, figsize=(15, rows * 5))
axes = axes.flatten()  # Flatten in case of 2D axes

# Loop through each column pair and create a scatter plot
for i, (col1, col2) in enumerate(columns_pairs):
    sns.scatterplot(x=data_numeric[col1], y=data_numeric[col2], ax=axes[i])
    axes[i].set_title(f"{col1} vs {col2}")


"""#### 3D triples #####"""

"""modled triples"""
columns_triples = list(combinations(modled_data.columns,3))
rows=len(columns_triples)
num_plots_triples=len(columns_triples)
triples_rows = -(-rows // cols)  # Calculate rows needed
#fig = plt.figure(figsize=(15,triples_rows*3))
fig , axes =plt.subplots(triples_rows,cols, figsize=(15, rows * 3))
axes= axes.flatten()
# Loop through each column triplet and create a 3D scatter plot
for i, (col1, col2, col3) in enumerate(columns_triples[:rows]):
    ax = fig.add_subplot(triples_rows, cols, i + 1, projection='3d')
    ax.scatter(modled_data[col1], modled_data[col2], modled_data[col3], alpha=0.7)
    ax.set_xlabel(col1)
    ax.set_ylabel(col2)
    ax.set_zlabel(col3)
    ax.set_title(f"{col1} vs {col2} vs {col3}")
    
plt.tight_layout()
plt.show()

"""measured triples """
columns_triples = list(combinations(measured_data.columns,3))
rows=len(columns_triples)
num_plots_triples=len(columns_triples)
triples_rows = -(-rows // cols)  # Calculate rows needed
#fig = plt.figure(figsize=(15,triples_rows*3))
fig , axes =plt.subplots(triples_rows,cols, figsize=(15, rows * 5))
axes= axes.flatten()
# Loop through each column triplet and create a 3D scatter plot
for i, (col1, col2, col3) in enumerate(columns_triples[:rows]):
    ax = fig.add_subplot(triples_rows, cols, i + 1, projection='3d')
    ax.scatter(measured_data[col1], measured_data[col2], measured_data[col3], alpha=0.7)
    ax.set_xlabel(col1)
    ax.set_ylabel(col2)
    ax.set_zlabel(col3)
    ax.set_title(f"{col1} vs {col2} vs {col3}")
    
plt.tight_layout()
plt.show()


"""////////////all combinaisons /////////////////"""

columns_triples = list(combinations(data_numeric.columns,3))
max_plots = min(len(columns_triples), 90)
num_plots_triples=len(columns_triples)
triples_rows = -(-max_plots // cols)  # Calculate rows needed
fig = plt.figure(figsize=(15,triples_rows*3))
#fig , axes =plt.subplots(triples_rows,cols,)
#axes= axes.floatten()
# Loop through each column triplet and create a 3D scatter plot
for i, (col1, col2, col3) in enumerate(columns_triples[:max_plots]):
    ax = fig.add_subplot(triples_rows, cols, i + 1, projection='3d')
    ax.scatter(data_numeric[col1], data_numeric[col2], data_numeric[col3], alpha=0.7)
    
    ax.set_xlabel(col1)
    ax.set_ylabel(col2)
    ax.set_zlabel(col3)
    ax.set_title(f"{col1} vs {col2} vs {col3}")

plt.tight_layout()
plt.show()
#/////////////////////3D normal ///////////////////////////////////

from mpl_toolkits.mplot3d import Axes3D
import matplotlib.animation as animation
import numpy as np


# Define columns to use for 3D plotting
x_col = "Mes frequency"   # Update with your column name
y_col = "Mes Average Power"# Update with your column name
z_col = "Mes. MSR"      # Update with your column name

# Create 3D figure
fig = plt.figure()
ax = fig.add_subplot(111, projection='3d')
ax.view_init(elev=20, azim=20,)

# Scatter plot
ax.scatter(data_numeric[x_col], data_numeric[y_col], data_numeric[z_col], c=data_numeric[z_col], cmap="viridis")

# Labels
ax.set_xlabel(x_col)
ax.set_ylabel(y_col)
ax.set_zlabel(z_col)
ax.set_title("3D Scatter Plot")

plt.show()


############# 3D gif #########

# Create 3D figure
fig = plt.figure(figsize=(8, 6))
ax = fig.add_subplot(111, projection='3d')
ax.view_init(elev=30, azim=45,)
# Line plot
ax.scatter(data_numeric[x_col], data_numeric[y_col], data_numeric[z_col],cmap="viridis")

# Labels
ax.set_xlabel(x_col)
ax.set_ylabel(y_col)
ax.set_zlabel(z_col)
ax.set_title("3D Line Plot")

# Function to rotate the plot
def rotate(angle):
    ax.view_init(elev=20, azim=angle)

# Create animation
ani = animation.FuncAnimation(fig, rotate, frames=np.arange(0, 360, 2), interval=50)

# Save as GIF
ani.save("3d_rotation.gif", writer=animation.PillowWriter(fps=20))

plt.show()


######### 3D responsive ##########

"""modeled combinaisons """
import plotly.express as px


columns_triples = list(combinations(modled_data.columns,3)) # combine all possible triples
"""scatter points"""
# Loop through each column triplet and display one plot at a time
for col1, col2, col3 in columns_triples[-10:]:
    fig = px.scatter_3d(modled_data, x=col1, y=col2, z=col3, opacity=0.7)
    fig.update_layout(title=f"{col1} vs {col2} vs {col3}")
    # Show interactive plot
    fig.show(renderer="browser")

"""measured combinaisons """
import plotly.express as px


columns_triples = list(combinations(measured_data.columns,3)) # combine all possible triples
"""scatter points"""
# Loop through each column triplet and display one plot at a time
for col1, col2, col3 in columns_triples[-10:]:
    fig = px.scatter_3d(modled_data, x=col1, y=col2, z=col3, opacity=0.7)
    fig.update_layout(title=f"{col1} vs {col2} vs {col3}")
    # Show interactive plot
    fig.show(renderer="browser")


    
    
    
"""/////////////////////////////////////////////"""
import plotly.express as px

#### drop unnecessar columns
filter = [col for col in data_avg if col.startswith("Mes")] # filter Mesured values 
data_avg = data_avg[filter] # select filtred data

columns_triples = list(combinations(data_avg.columns,3)) # combine all possible triples
"""scatter points"""
# Loop through each column triplet and display one plot at a time
for col1, col2, col3 in columns_triples[-10:]:
    fig = px.scatter_3d(data_avg, x=col1, y=col2, z=col3, opacity=0.7)
    fig.update_layout(title=f"{col1} vs {col2} vs {col3}")
    # Show interactive plot
    fig.show(renderer="browser")

    # Wait for user input before showing the next plot
    input("Press Enter to show the next plot...")