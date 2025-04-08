import openpyxl
import sys

def add_value_to_excel(excel_file, column_num, value):
    # Load the workbook and select the sheet
    workbook = openpyxl.load_workbook(excel_file)
    sheet = workbook.active  # Or use workbook['SheetName'] for a specific sheet

    # Convert column number to letter
    column_letter = chr(64 + column_num)  # For column 1 = 'A', column 2 = 'B', etc.

    # Get the latest row (last row with data in any column)
    latest_row = sheet.max_row

    # Check if the specified column in the latest row is empty
    target_cell = sheet[f"{column_letter}{latest_row}"]
    if target_cell.value is None:  # If the cell is empty
        sheet[f"{column_letter}{latest_row}"] = value
    else:  # If the cell is not empty, move to the next row
        sheet[f"{column_letter}{latest_row + 1}"] = value
        latest_row += 1  # Update the latest row for the print statement

    # Save the workbook
    workbook.save(excel_file)
    print(f"Value '{value}' added to column {column_letter} at row {latest_row}")

# Command line arguments: python add_to_excel.py <excel_file> <column_number> <value>
if __name__ == "__main__":
    if len(sys.argv) != 4:
        print("Usage: python add_to_excel.py <excel_file> <column_number> <value>")
        sys.exit(1)

    excel_file = sys.argv[1]
    column_num = int(sys.argv[2])
    value = sys.argv[3]

    add_value_to_excel(excel_file, column_num, value)
