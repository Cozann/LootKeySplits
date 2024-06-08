package com.lootkeysplit;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;

public class ResetButton extends JButton
{
    public ResetButton(String text)
    {
        super(text);
        setFocusable(false);
    }

    public void addMouseButton1PressedHandler(Runnable callback)
    {
        addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent e)
            {
                if (e.getButton() == MouseEvent.BUTTON1)
                {
                    callback.run();
                }
            }
        });
    }
    public void addMouseButton2PressedHandler(Runnable callback)
    {
        addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent e)
            {
                if (e.getButton() == MouseEvent.BUTTON1)
                {
                    callback.run();
                }
            }
        });
    }
    public void addAdvanceButtonHandler(Runnable callback)
    {
        addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent e)
            {
                if (e.getButton() == MouseEvent.BUTTON1)
                {
                    callback.run();
                }
            }
        });
    }
}