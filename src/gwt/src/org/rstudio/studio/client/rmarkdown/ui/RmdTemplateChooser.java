/*
 * RmdTemplateChooser.java
 *
 * Copyright (C) 2009-14 by RStudio, Inc.
 *
 * Unless you have received this program directly from RStudio pursuant
 * to the terms of a commercial license agreement with RStudio, then
 * this program is licensed to you under the terms of version 3 of the
 * GNU Affero General Public License. This program is distributed WITHOUT
 * ANY EXPRESS OR IMPLIED WARRANTY, INCLUDING THOSE OF NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. Please refer to the
 * AGPL (http://www.gnu.org/licenses/agpl-3.0.txt) for more details.
 *
 */
package org.rstudio.studio.client.rmarkdown.ui;

import java.util.ArrayList;

import org.rstudio.core.client.files.FileSystemItem;
import org.rstudio.core.client.resources.CoreResources;
import org.rstudio.core.client.widget.CaptionWithHelp;
import org.rstudio.core.client.widget.DirectoryChooserTextBox;
import org.rstudio.core.client.widget.Operation;
import org.rstudio.core.client.widget.OperationWithInput;
import org.rstudio.core.client.widget.SimplePanelWithProgress;
import org.rstudio.core.client.widget.WidgetListBox;
import org.rstudio.studio.client.RStudioGinjector;
import org.rstudio.studio.client.rmarkdown.RmdTemplateDiscovery;
import org.rstudio.studio.client.rmarkdown.model.RmdChosenTemplate;
import org.rstudio.studio.client.rmarkdown.model.RmdDiscoveredTemplate;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class RmdTemplateChooser extends Composite
{
   private static RmdTemplateChooserUiBinder uiBinder = GWT
         .create(RmdTemplateChooserUiBinder.class);

   interface RmdTemplateChooserUiBinder extends
         UiBinder<Widget, RmdTemplateChooser>
   {
   }

   public RmdTemplateChooser()
   {
      initWidget(uiBinder.createAndBindUi(this));
      dirLocation_.setText(FileSystemItem.HOME_PATH);
      listTemplates_.setItemPadding(2, Unit.PX);
      listTemplates_.addChangeHandler(new ChangeHandler()
      {
         @Override
         public void onChange(ChangeEvent evt)
         {
            // when the template changes, update the check to correspond to the
            // template's preference, if any
            RmdDiscoveredTemplateItem item = listTemplates_.getSelectedItem();
            chkCreate_.setValue(
                  item.getTemplate().getCreateDir().equals("true"));
         }
      });
   }
   
   public void populateTemplates()
   {
      if (state_ != STATE_EMPTY)
         return;
      
      progressPanel_.showProgress(250);
      discovery_ = RStudioGinjector.INSTANCE.getRmdTemplateDiscovery();
      discovery_.discoverTemplates(
         new OperationWithInput<RmdDiscoveredTemplate>()
         {
            @Override
            public void execute(RmdDiscoveredTemplate template)
            {
               // create a template list item from the template; add it at the
               // end if it isn't the user's preferred template
               listTemplates_.addItem(new RmdDiscoveredTemplateItem(template), 
                     !template.getPath().equals(
                           discovery_.getRmdPreferredTemplatePath()));
               templates_.add(template);
            }
         },
         new Operation()
         {
            @Override
            public void execute()
            {
               state_ = STATE_POPULATED;
               completeDiscovery();
            }
         });
      state_ = STATE_POPULATING;
   }
   
   public RmdChosenTemplate getChosenTemplate()
   {
      return new RmdChosenTemplate(getSelectedTemplatePath(), 
                                   getFileName(), 
                                   getDirectory(), 
                                   createDirectory());
   }
   
   public int getState()
   {
      return state_;
   }
   
   // UI factory methods ------------------------------------------------------

   @UiFactory
   public DirectoryChooserTextBox makeDirectoryChooserTextbox()
   {
      return new DirectoryChooserTextBox("", null);
   }
   
   @UiFactory
   public CaptionWithHelp makeHelpCaption()
   {
      return new CaptionWithHelp("Template:", "Using R Markdown Templates",
                                 "using_rmarkdown_templates");
   }
   
   @UiFactory
   public SimplePanelWithProgress makeProgressPanel()
   {
      return new SimplePanelWithProgress(new Image(
            CoreResources.INSTANCE.progress()), 50);
   }
   
   // Private methods ---------------------------------------------------------
   
   private void completeDiscovery()
   {
      if (listTemplates_.getItemCount() == 0)
      {
         // no templates found -- hide UI and show message
         progressPanel_.setVisible(false);
         noTemplatesFound_.setVisible(true);
         txtName_.setEnabled(false);
         dirLocation_.setEnabled(false);
         chkCreate_.setEnabled(false);
      }
      else
      {
         // templates found -- enable creation UI
         progressPanel_.setWidget(listTemplates_);
         templateOptionsPanel_.setVisible(true);
         RmdDiscoveredTemplate template = 
               listTemplates_.getItemAtIdx(0).getTemplate();
         if (template != null)
         {
            chkCreate_.setValue(template.getCreateDir().equals("true"));
         }
      }
   }

   private String getSelectedTemplatePath()
   {
      RmdDiscoveredTemplateItem item = listTemplates_.getSelectedItem();
      if (item != null)
         return item.getTemplate().getPath();
      return null;
   }
   
   private String getFileName()
   {
      return txtName_.getText();
   }
   
   private String getDirectory()
   {
      return dirLocation_.getText();
   }
   
   private boolean createDirectory()
   {
      return chkCreate_.getValue();
   }
   
   private RmdTemplateDiscovery discovery_;
   private int state_;
   private ArrayList<RmdDiscoveredTemplate> templates_ = 
         new ArrayList<RmdDiscoveredTemplate>();
   
   @UiField WidgetListBox<RmdDiscoveredTemplateItem> listTemplates_;
   @UiField TextBox txtName_;
   @UiField DirectoryChooserTextBox dirLocation_;
   @UiField CheckBox chkCreate_;
   @UiField HTMLPanel noTemplatesFound_;
   @UiField HTMLPanel templateOptionsPanel_;
   @UiField SimplePanelWithProgress progressPanel_;
   
   public final static int STATE_EMPTY = 0;
   public final static int STATE_POPULATING = 1;
   public final static int STATE_POPULATED = 2;
}
