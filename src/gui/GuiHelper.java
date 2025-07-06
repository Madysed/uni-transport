package gui;

import javax.swing.*;

public class GuiHelper {
    // Helper class for displaying checkbox in list
    public static class CheckBoxListCellRenderer extends JCheckBox implements ListCellRenderer<JCheckBox> {
        @Override
        public java.awt.Component getListCellRendererComponent(JList<? extends JCheckBox> list, JCheckBox value,
                                                               int index, boolean isSelected, boolean cellHasFocus) {
            setEnabled(list.isEnabled());
            setSelected(value.isSelected());
            setFont(list.getFont());
            setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
            setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
            setText(value.getText());
            return this;
        }
    }


}
