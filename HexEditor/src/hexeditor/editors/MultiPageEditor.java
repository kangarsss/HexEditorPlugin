package hexeditor.editors;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.*;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.osgi.framework.Bundle;
import org.eclipse.ui.ide.IDE;

/**
 * A hexadecimal editor main class.
 * 
 * page 0 contains a nested text editor. page 1 allows you to change the font
 * used in page 2 page 2 shows the words in page 0 in a special font page 3
 * shows hexadecimal editor
 * 
 */
public class MultiPageEditor extends MultiPageEditorPart implements
		IResourceChangeListener {

	/** The text editor used in page 0. */
	private TextEditor editor;
	private TableEditor tableEditor;

	/** The text widget used in page 2. */
	private StyledText text;

	private Table table;

	private String hexText;
	
	private ArrayList<String> data;

	private Composite tableComposite;

	private Label fileSizeLabel;

	private int testint = 0;
	
	private ArrayList<Label> labelList = new ArrayList<Label>();
	int bitRow = 0;
	int bitCol = 1;
	
    private Bundle bundle = Platform.getBundle("HexEditor");
    private URL xDigitsClockURL = bundle.getEntry("xDigitsClock.ttf");
    private URL xDigitsSansURL = bundle.getEntry("xDigitsSans.ttf");  
    
    private InputStream xDigitsClockIs = getClass().getClassLoader().getResourceAsStream("/xDigitsClock.ttf");
    private InputStream xDigitsSansIs = getClass().getClassLoader().getResourceAsStream("/xDigitsSans.ttf");
    
    private String fontName = null;
    
    private org.eclipse.swt.graphics.Font font;
    
    private PluginMethods pm = new PluginMethods();

	/**
	 * Creates a hex editor example.
	 */
	public MultiPageEditor() {
		super();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}

	/**
	 * Creates page 0 of the hex editor, which contains a text editor.
	 */
	void createPage0() {
		try {
			editor = new TextEditor();
			int index = addPage(editor, getEditorInput());
			setPageText(index, editor.getTitle());
		} catch (PartInitException e) {
			ErrorDialog.openError(getSite().getShell(),
					"Error creating nested text editor", null, e.getStatus());
		}
	}

	/**
	 * Creates page 2 of the hex editor, which shows the sorted text.
	 * @throws IOException 
	 * @throws FontFormatException 
	 */
	void createPage2() throws FontFormatException, IOException {
		Composite composite = new Composite(getContainer(), SWT.NONE);
		FillLayout layout = new FillLayout();
		composite.setLayout(layout);
		
		getFonts();
		
		fontName = Font.createFont(Font.TRUETYPE_FONT, xDigitsSansIs).deriveFont(12).getFontName();
		composite.getDisplay().loadFont(FileLocator.toFileURL(xDigitsSansURL).getPath());
		font = new org.eclipse.swt.graphics.Font(composite.getDisplay(), fontName, 12, SWT.NORMAL);
		
		
		text = new StyledText(composite, SWT.H_SCROLL | SWT.V_SCROLL);
		text.setEditable(false);
		int index = addPage(composite);
		setPageText(index, "Preview");
	}

	/**
	 * Creates page 3 of the hex editor, which shows the table.
	 */
	void createPage3() {
		
		getFonts();

		tableComposite = new Composite(getContainer(), SWT.BORDER);
		FillLayout fillLayout = new FillLayout(SWT.VERTICAL);
		tableComposite.setLayout(fillLayout);

		Composite outer = new Composite(tableComposite, SWT.BORDER);
		outer.setBackground(null);

		FormLayout formLayout = new FormLayout();
		outer.setLayout(formLayout);

		Composite innerBottom = new Composite(outer, SWT.BORDER);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 16;

		GridData gridData = new GridData();
		gridData.horizontalIndent = 10;
		gridData.grabExcessVerticalSpace = true;
		gridData.widthHint = 200;
		
		GridData gridData2 = new GridData();
		gridData2.horizontalIndent = 1;
		gridData2.grabExcessVerticalSpace = true;
		gridData2.widthHint = 10;

		innerBottom.setLayout(gridLayout);
		innerBottom.setLayoutData(gridData);
		innerBottom.setBackground(null);

		FormData fData = new FormData();
		fData.top = new FormAttachment(92);
		fData.left = new FormAttachment(0);
		fData.right = new FormAttachment(100);
		fData.bottom = new FormAttachment(100);

		innerBottom.setLayoutData(fData);

		Composite innerTop = new Composite(outer, SWT.BORDER);
		innerTop.setLayout(fillLayout);
		innerTop.setBackground(null);

		fData = new FormData();
		fData.top = new FormAttachment(0);
		fData.left = new FormAttachment(0);
		fData.right = new FormAttachment(100);
		fData.bottom = new FormAttachment(innerBottom);
		innerTop.setLayoutData(fData);

		Label fileNameLabel = new org.eclipse.swt.widgets.Label(innerBottom,SWT.BORDER);
		fileNameLabel.setLayoutData(gridData);

		fileSizeLabel = new org.eclipse.swt.widgets.Label(innerBottom,SWT.BORDER);
		fileSizeLabel.setLayoutData(gridData);
		
		for(int i = 0; i < 8; i++){
			final Label bit1 = new Label(innerBottom,SWT.BORDER);
			bit1.setText("0");
			bit1.setLayoutData(gridData2);
			bit1.addMouseListener(new MouseListener() {
				
				public void mouseUp(MouseEvent arg0) {
					if(bit1.getText().equals("0")){
						bit1.setText("1");
						
					}
					else{
						bit1.setText("0");
					}
					
				}
				
				public void mouseDown(MouseEvent arg0) {
					
					
				}
				
				public void mouseDoubleClick(MouseEvent arg0) {
					
					
				}
			});
			labelList.add(bit1);
		}
		
		final Button changeBit = new Button(innerBottom, SWT.BORDER);
		changeBit.setText("Change");
		changeBit.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent event) {
				String bitText = "";
				for( int i = 0; i < 8; i++){
					bitText += labelList.get(i).getText();
				}
				String rez = Long.toHexString(Long.parseLong(bitText,2));
				String editorT = editor.getDocumentProvider()
						.getDocument(editor.getEditorInput()).get();
				StringBuilder builderText = new StringBuilder(editorT);
				int i = Integer.parseInt(rez, 16);
				builderText.setCharAt(bitRow * 16 + bitCol -1 + testint, (char) i);
				editor.getDocumentProvider()
						.getDocument(editor.getEditorInput())
						.set(builderText.toString());
				
				showFontTable(testint);
				
				Control old = tableEditor.getEditor();
				old.dispose();

			}

			public void widgetDefaultSelected(SelectionEvent event) {

			}

		});

		GridData offsetLabelData = new GridData();
		offsetLabelData.horizontalIndent = 20;

		Label searchOffsetLabel = new Label(innerBottom, SWT.BORDER);
		searchOffsetLabel.setLayoutData(offsetLabelData);

		GridData searchFieldData = new GridData();
		searchFieldData.heightHint = 12;
		searchFieldData.widthHint = 70;

		final Text searchTextField = new Text(innerBottom, SWT.BORDER);
		searchTextField.setLayoutData(searchFieldData);
		searchTextField.addListener(SWT.Traverse, new Listener() {

			public void handleEvent(Event e) {
				if (e.detail == SWT.TRAVERSE_RETURN) {
					gotoOffset(searchTextField.getText());
				}

			}
		});

		fileNameLabel.setText("File name: " + editor.getTitle());

		GridData btnInsertData = new GridData();
		btnInsertData.horizontalIndent = 10;
		btnInsertData.heightHint = 30;
		btnInsertData.widthHint = 130;

		Button btnInsert = new Button(innerBottom, SWT.BORDER);
		btnInsert.setText("Insert text");
		btnInsert.setLayoutData(btnInsertData);
		btnInsert.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent event) {

				Composite composite = new Composite(getContainer(), SWT.NONE);
				if (table.getItem(table.getSelectionIndex()).getText(17).length() == 16){
					InputDialog dlg = new InputDialog(composite.getShell(), "", 
													"Input characters", "thisIsAnExample!",
													 new LengthValidator());
					if (dlg.open() == Window.OK) {
						TableItem ti = new TableItem(table, SWT.NONE);
						String normal = dlg.getValue();
						String hex = pm.getFontString(normal);

						for (int i = table.getItemCount() - 1; i > table.getSelectionIndex(); i--) {
							for (int j = 0; j < table.getColumnCount(); j++) {
								if (j == 0) {
									table.getItem(i).setText(0,
											pm.generateOffset(i + (testint / 16)));
								} 
								else if (i == table.getSelectionIndex() + 1) {
									if (j == table.getColumnCount() - 1)
										table.getItem(i).setText(j, normal);
									else
										table.getItem(i).setText(
												j,
												String.valueOf(hex
														.charAt(j - 1)));
								} 
								else {
									String str = table.getItem(i - 1).getText(j);
									table.getItem(i).setText(j, str);
								}
							}
						}
						appendFile(normal, table.getSelectionIndex() + 1);
						searchTextField.setText("");
						checkFileSize();
						table.redraw();
					}
				} 
				else {
					MessageBox dialog = new MessageBox(composite.getShell(),
							SWT.ICON_ERROR | SWT.OK);
					dialog.setText("Title");
					dialog.setMessage("Cant input, last line length must be 16!");
					dialog.open();
				}

			}

			public void widgetDefaultSelected(SelectionEvent event) {
			}

		});

		GridData btnFontData = new GridData();
		btnFontData.horizontalIndent = 10;

		Label btnFontLabel = new Label(innerBottom, SWT.BORDER);
		btnFontLabel.setLayoutData(btnFontData);
		btnFontLabel.setText("Font: ");

		final Button btnChangeFont = new Button(innerBottom, SWT.BORDER);
		btnChangeFont.setText("xDigitsClock");
		btnChangeFont.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent event) {
				
				getFonts();

				if (btnChangeFont.getText().equals("xDigitsClock")) {	
					
					try {
						fontName = Font.createFont(Font.TRUETYPE_FONT, xDigitsSansIs).deriveFont(12).getFontName();
						tableComposite.getDisplay().loadFont(FileLocator.toFileURL(xDigitsSansURL).getPath());
						font = new org.eclipse.swt.graphics.Font(text.getDisplay(), fontName, 10, SWT.NORMAL);
					} catch (FontFormatException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					btnChangeFont.setText("xDigitsSans");
					
				} else {
					
					try {
						fontName = Font.createFont(Font.TRUETYPE_FONT, xDigitsClockIs).deriveFont(12).getFontName();
						tableComposite.getDisplay().loadFont(FileLocator.toFileURL(xDigitsClockURL).getPath());
						font = new org.eclipse.swt.graphics.Font(text.getDisplay(), fontName, 10, SWT.NORMAL);
					} catch (FontFormatException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					btnChangeFont.setText("xDigitsClock");
					
				}

				table.setFont(font);
				table.redraw();

			}

			public void widgetDefaultSelected(SelectionEvent event) {

			}

		});

		checkFileSize();

		searchOffsetLabel.setText("Go to: ");

		int index = addPage(tableComposite);
		setPageText(index, "Hex Table");

		table = new Table(innerTop, SWT.SINGLE | SWT.HIDE_SELECTION | SWT.FULL_SELECTION);

		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		generateColumns();

		tableEditor = new TableEditor(table);
		tableEditor.horizontalAlignment = SWT.LEFT;
		tableEditor.grabHorizontal = true;

		table.addKeyListener(new KeyListener() {

			public void keyReleased(KeyEvent arg0) {

			}

			public void keyPressed(KeyEvent arg0) {

				if (arg0.keyCode == SWT.ARROW_DOWN
						&& table.getSelectionIndex() == 36) {
					if (!(testint + 593 > editor.getDocumentProvider()
							.getDocument(editor.getEditorInput()).get()
							.length())) {

						testint += 16;
						
						try {
							showFontTable2(testint);
						} catch (FontFormatException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
						
						table.setSelection(36);
					}
				}

				if (arg0.keyCode == SWT.ARROW_UP
						&& table.getSelectionIndex() == 0) {
					if (testint > 0) {
						testint -= 16;
						
						showFontTable(testint);
						
						table.setSelection(1);
					}
				}

			}
		});

		table.addListener(SWT.MouseDoubleClick, new Listener() {

			public void handleEvent(Event event) {

				Control old = tableEditor.getEditor();
				if (old != null)
					old.dispose();

				Point pt = new Point(event.x, event.y);

				final TableItem item = table.getItem(pt);

				if (item == null) {
					return;
				}

				int column = -1;
				int index = -1;
				for (int i = 0, n = table.getColumnCount(); i < n; i++) {
					Rectangle rect = item.getBounds(i);
					if (rect.contains(pt)) {
						column = i;
						index = table.indexOf(item);
						break;
					}
				}

				if (column == -1 || column == 0) {
					return;
				}
				setTableData(table, item, index, column);

			}

		});

		table.addListener(SWT.MouseDown, new Listener() {

			public void handleEvent(Event event) {

				Control old = tableEditor.getEditor();
				if (old != null)
					old.dispose();

			}
		});

	}
	
	/**
	 * Puts data into table
	 * @param table - a table to put in
	 * @param item - table row
	 * @param index - row index
	 * @param column - column index
	 */
	public void setTableData(final Table table, final TableItem item,
			final int index, int column) {
		checkFileSize();

		final Text text = new Text(table, SWT.NONE);

		pm.beforeText = item.getText(column);

		if (column != 17 && !item.getText(column).equals("")) {
			hexText = Integer
					.toHexString((int) item.getText(column).charAt(0) - 57360);
			text.setText(hexText);
		} 
		else {
			text.setText(item.getText(column));
		}

		text.selectAll();
		text.setFocus();

		tableEditor.minimumWidth = text.getBounds().width;

		tableEditor.setEditor(text, item, column);

		final int col = column;
		final int itemc = index * 16 + col - 1;
		final int row = index;
		bitRow = index;
		bitCol = column;
		final int testint2 = testint;

		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent event) {
				
				String editorT = editor.getDocumentProvider().getDocument(editor.getEditorInput()).get();
				StringBuilder builderText = new StringBuilder(editorT);

				if (col == 17) {
					if (row != table.getItemCount() - 1 && text.getText().length() == 16) {
						changeRowData(item, row, col, text);
						writeToFile(text.getText(), row);
					} 
					else if (row == table.getItemCount() - 1 && text.getText().length() <= 16) {
						changeRowData(item, row, col, text);
						for (int i = text.getText().length() + 1; i < 17; i++)
							table.getItem(row).setText(i, "");

						writeToFile(text.getText(), row);
					}

				} 
				else if (text.getText().equals("") && itemc + testint2 == builderText.length() - 1) {

					String tempt = builderText.toString();
					item.setText(col, "");
					item.setBackground(col, new org.eclipse.swt.graphics.Color(
							tableComposite.getDisplay(), 245, 201, 208));
					tempt = tempt.substring(0, itemc + testint2)
							+ tempt.substring(itemc + 1 + testint2,
									tempt.length());
					editor.getDocumentProvider()
							.getDocument(editor.getEditorInput()).set(tempt);

					String sentence = table.getItem(row).getText(17);
					StringBuilder sb = new StringBuilder(sentence);
					sb.deleteCharAt(sb.length() - 1);
					table.getItem(row).setText(17, String.valueOf(sb));
					table.redraw();

					Control old = tableEditor.getEditor();
					old.dispose();
					if (col == 16) {
						if (table.getItemCount() == index + 2) {
							table.remove(index + 1);
						}
						if (index == 23) {
							if (testint > 0) {
								testint -= 16;
								showFontTable(testint);
							}
						}
					}
					
					if (col == 1) {
						TableItem item2 = table.getItem(index - 1);
						setTableData(table, item2, index - 1, 16);
					} 
					else {
						setTableData(table, item, index, col - 1);
					}
				} 
				else if (text.getText().length() == 2) {

					String changedText = pm.getTableString(text.getText());
					item.setText(col, changedText);
					item.setBackground(col, new org.eclipse.swt.graphics.Color(
							tableComposite.getDisplay(), 245, 201, 208));
					int i = Integer.parseInt(text.getText(), 16);

					if (itemc + testint2 + 1 == builderText.length()) {
						if (col == 16) {
							TableItem tb = new TableItem(table, SWT.NONE);
							tb.setText(0, pm.generateOffset(row + 1 + testint / 16));
							table.setSelection(table.getSelectionIndex() + 1);
						}
					}
					
					if(itemc + testint2 < builderText.length()){
						builderText.setCharAt(itemc + testint2, (char) i);
						editor.getDocumentProvider()
								.getDocument(editor.getEditorInput())
								.set(builderText.toString());
						text.setText(item.getText(col));

						char ch = table.getItem(row).getText(col).charAt(0);
						String hexCh = "\\u" + Integer.toHexString((int) ch - 57360);
						ch = (char) Integer.parseInt(hexCh.substring(2), 16);
						String sentence = table.getItem(row).getText(17);
						StringBuilder sb = new StringBuilder(sentence);
						sb.setCharAt(col - 1, ch);
						table.getItem(row).setText(17, String.valueOf(sb));
						table.redraw();

						Control old = tableEditor.getEditor();
						old.dispose();
						if (col + 1 == 17) {
							TableItem item2 = table.getItem(index + 1);
							setTableData(table, item2, index + 1, 1);
						} 
						else {
							item.setBackground(col, new org.eclipse.swt.graphics.Color(tableComposite.getDisplay(), 245,201, 208));
							setTableData(table, item, index, col + 1);
						}

					} 
					else if (itemc + testint2 == builderText.length()) {
						builderText.append((char) i);
						if (col == 16) {
							TableItem tb = new TableItem(table, SWT.NONE);
							tb.setText(0,
									pm.generateOffset(row + 1 + testint / 16));
							table.setSelection(table.getSelectionIndex() + 1);
							
						}
					}
					
					editor.getDocumentProvider()
							.getDocument(editor.getEditorInput())
							.set(builderText.toString());
					boolean append = true;
					
					try{
						text.setText(item.getText(col));
					}catch(Exception e){
						append = false;
					}
					
					if(append == true){
						char ch = table.getItem(row).getText(col).charAt(0);
						String hexCh = "\\u"
								+ Integer.toHexString((int) ch - 57360);
						ch = (char) Integer.parseInt(hexCh.substring(2), 16);
						String sentence = table.getItem(row).getText(17);
						StringBuilder sb = new StringBuilder(sentence);
						
						sb.append(ch);		 
	
						table.getItem(row).setText(17, String.valueOf(sb));
						table.redraw();
	
						Control old = tableEditor.getEditor();
						old.dispose();
	
						if (col + 1 == 17) {
							TableItem item2 = table.getItem(index + 1);
							setTableData(table, item2, index + 1, 1);
						} else {
							setTableData(table, item, index, col + 1);
						}
					}
				}

			}
		});
	}

	
	/**
	 * Creates pages of the hex editor.
	 */
	protected void createPages() {
		createPage0();
		
		try {
			createPage2();
		} catch (FontFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		createPage3();
	}

	/**
	 * The MultiPageEditorPart implementation of this
	 * IWorkbenchPart method disposes all nested editors.
	 * Subclasses may extend.
	 */
	public void dispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		super.dispose();
	}

	/**
	 * Saves the multi-page editor's document.
	 */
	public void doSave(IProgressMonitor monitor) {
		getEditor(0).doSave(monitor);
	}

	/**
	 * Saves the multi-page editor's document as another file. Also updates the
	 * text for page 0's tab, and updates this multi-page editor's input to
	 * correspond to the nested editor's.
	 */
	public void doSaveAs() {
		IEditorPart editor = getEditor(0);
		editor.doSaveAs();
		setPageText(0, editor.getTitle());
		setInput(editor.getEditorInput());
	}

	/*
	 * (non-Javadoc) Method declared on IEditorPart
	 */
	public void gotoMarker(IMarker marker) {
		setActivePage(0);
		IDE.gotoMarker(getEditor(0), marker);
	}

	/**
	 * The MultiPageEditorExample implementation of this method
	 * checks that the input is an instance of IFileEditorInput.
	 */
	public void init(IEditorSite site, IEditorInput editorInput)
			throws PartInitException {
		if (!(editorInput instanceof IFileEditorInput))
			throw new PartInitException(
					"Invalid Input: Must be IFileEditorInput");
		super.init(site, editorInput);
	}

	/*
	 * (non-Javadoc) Method declared on IEditorPart.
	 */
	public boolean isSaveAsAllowed() {
		return true;
	}

	/**
	 * Calculates the contents of pages when they are activated.  
	 */
	protected void pageChange(int newPageIndex) {
		super.pageChange(newPageIndex);
		if (newPageIndex == 1) {
			
			try {
				sortWords();
			} catch (FontFormatException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		if (newPageIndex == 2) {
			showFontTable(testint);
			table.setSelection(0);
			checkFileSize();
		}
	}

	/**
	 * Closes all project files on project close.
	 */
	public void resourceChanged(final IResourceChangeEvent event) {
		if (event.getType() == IResourceChangeEvent.PRE_CLOSE) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					IWorkbenchPage[] pages = getSite().getWorkbenchWindow()
							.getPages();
					for (int i = 0; i < pages.length; i++) {
						if (((FileEditorInput) editor.getEditorInput())
								.getFile().getProject()
								.equals(event.getResource())) {
							IEditorPart editorPart = pages[i].findEditor(editor
									.getEditorInput());
							pages[i].closeEditor(editorPart, true);
						}
					}
				}
			});
		}
	}

	/**
	 * Sets the font related data to be applied to the text in page 2.
	 * @throws IOException 
	 * @throws FontFormatException 
	 */
	public void setFont() throws FontFormatException, IOException {
		FontDialog fontDialog = new FontDialog(getSite().getShell());
		fontDialog.setFontList(text.getFont().getFontData());
		FontData fontData = fontDialog.open();
		if (fontData != null) {
			if (font != null)
				font.dispose();
			fontName = Font.createFont(Font.TRUETYPE_FONT, xDigitsSansIs).deriveFont(12).getFontName();
			font = new org.eclipse.swt.graphics.Font(text.getDisplay(), fontName, 10, SWT.NORMAL);
			text.setFont(font);
		}
	}

	/**
	 * Sorts the words in page 0, and shows them in page 2.
	 * @throws IOException 
	 * @throws FontFormatException 
	 */
	void sortWords() throws FontFormatException, IOException {
		getFonts();

		String editorText = editor.getDocumentProvider()
				.getDocument(editor.getEditorInput()).get();

		text.setText(editorText.toString());
		
		fontName = Font.createFont(Font.TRUETYPE_FONT, xDigitsSansIs).deriveFont(12).getFontName();
		font = new org.eclipse.swt.graphics.Font(text.getDisplay(), fontName, 10, SWT.NORMAL);
		
		text.setFont(font);
		hexText = text.getText();

		String temp = pm.getFontString(hexText);

		text.setText(temp);
	}
	
	/**
	 * Fills table with data
	 * @param beginIndex - text character index to start from
	 * @throws FontFormatException
	 * @throws IOException
	 */
	void showFontTable2(int beginIndex) throws FontFormatException, IOException {
		getFonts();
		
		String editorText = editor.getDocumentProvider()
				.getDocument(editor.getEditorInput()).get();

		if (editorText.length() >= 592) {
			if (beginIndex + 592 > editorText.length()) {
				editorText = editorText.substring(beginIndex,
						editorText.length());
			} else {
				editorText = editorText.substring(beginIndex, beginIndex + 592);
			}
		} else {
			editorText = editorText.substring(beginIndex, editorText.length());
		}

		text.setText(editorText.toString());
		hexText = text.getText();
		
		fontName = Font.createFont(Font.TRUETYPE_FONT, xDigitsSansIs).deriveFont(12).getFontName();
		font = new org.eclipse.swt.graphics.Font(text.getDisplay(), fontName, 10, SWT.NORMAL);
		
		table.setFont(font);

		table.removeAll();

		data = new ArrayList<String>();

		editorText = editorText.replace("\n", "/").replace("\r", "/");
		
		String splittedString[] = editorText.split("(?<=\\G.{" + 16 + "})");

		int counter = 0;
		int idx = 0;

		data.add(pm.generateOffset(testint / 16));
		for (int i = 0; i < pm.getFontString(hexText).length(); i++) {
			data.add(String.valueOf(pm.getFontString(hexText).charAt(i)));
			counter++;
			if (counter == 16) {
				data.add(splittedString[idx]);
				idx++;
				counter = 0;
			}

		}

		counter = 0;
		int lineCounter = 1;
		for (int i = 0; i < data.size(); i++) {
			if (counter == 18) {
				data.add(i, pm.generateOffset(lineCounter + (testint / 16)));
				counter = 0;
				lineCounter++;
			}
			counter++;
		}

		int something = 0;
		if (!data.get(data.size() - 1).equals(
				splittedString[splittedString.length - 1])) {
			for (int i = data.size() - 1;; i--) {
				if (data.get(i).charAt(0) >= 48 && data.get(i).charAt(0) <= 122)
					break;

				something = i;
			}

			for (int i = data.size(); i < something + 16; i++)
				data.add("");

			data.add(splittedString[splittedString.length - 1]);
		}

		int dataSize = data.size();
		int j = 0, key = 1;

		while (dataSize > 0) {
			int index = 0;

			if (dataSize <= 18) {
				TableItem item1 = new TableItem(table, SWT.CENTER);
				for (int i = j * 18; i < data.size(); i++) {
					item1.setText(index, data.get(i));
					index++;
				}

				dataSize = 0;
			} else if (dataSize > 18) {

				TableItem item1 = new TableItem(table, SWT.CENTER);
				for (int i = data.size() - dataSize; i < key * 18; i++) {
					item1.setText(index, data.get(i));
					index++;
				}

				dataSize -= 18;
				j++;
			}

			key++;

		}

	}

	/**
	 * Generates table columns
	 */
	public void generateColumns() {
		char ch = 65;
		for (int loopIndex = 0; loopIndex < 18; loopIndex++) {
			TableColumn column = new TableColumn(table, SWT.CENTER);
			column.setResizable(true);
			if (loopIndex == 0) {
				column.setWidth(90);
				column.setText("OFFSET");
			} else if (loopIndex == 17) {
				column.setText("ASCII");
				column.setWidth(40);
			} else if (loopIndex > 10) {
				column.setText(String.valueOf(ch));
				ch++;
				column.setWidth(50);
			} else {
				column.setText(String.valueOf(loopIndex - 1));
				column.setWidth(50);

			}
		}
	}

	
	/**
	 * Help method for getFontString(String hexText) method
	 * @param text - text to transformed
	 * @return
	 */
	public String getHexString(Text text) {
		String hexText = text.getText();

		return pm.getFontString(hexText);
	}

	
	/**
	 * Searches offset by given parameter
	 * @param str - offset
	 */
	public void gotoOffset(String str) {
		String testAddress = "0";

		if (str.length() == 8) {
			for (int i = 0; i < str.length(); i++) {
				if (!String.valueOf(str.charAt(i)).equals("0")) {
					testAddress = str.substring(i, str.length() - 1);
					break;
				}
			}
			String file = editor.getDocumentProvider()
					.getDocument(editor.getEditorInput()).get();
			if (file.length() >= Integer.decode("0x" + testAddress) * 16) {
				testint = Integer.decode("0x" + testAddress) * 16;
				showFontTable(testint);
				table.setSelection(0);
			}
		} else {
			MessageBox dialog = new MessageBox(tableComposite.getShell(),
					SWT.ICON_ERROR | SWT.OK);
			dialog.setText("Title");
			dialog.setMessage("OFFSET must be 8 characters long!");
			dialog.open();
		}

	}
	
	/**
	 * Changes file content
	 * @param newText - text to add
	 * @param row - row index
	 */
	public void writeToFile(String newText, int row) {
		int end = row * 16;

		String originalText = "";
		String temp = "";
		for (int i = 0; i < table.getItemCount(); i++) {
			temp = table.getItem(i).getText(17);
			for (int j = 0; j < temp.length(); j++)
				if (temp.charAt(j) == '/')
					temp = temp.substring(0, j) + "\n" + temp.substring(j + 1);

			originalText += temp;
		}

		temp = originalText;
		temp = temp.substring(0, end) + newText;

		if (temp.length() < originalText.length()) {
			originalText = originalText.substring(temp.length());
			temp += originalText;
		}
		editor.getDocumentProvider().getDocument(editor.getEditorInput())
				.set(temp);
	}
	
	/**
	 * Appends new lines from table to file
	 * @param newText - text to add
	 * @param row - row index
	 */
	public void appendFile(String newText, int row) {
		int end = row * 16 + testint;
		String originalText = editor.getDocumentProvider()
				.getDocument(editor.getEditorInput()).get();
		String temp = originalText;

		temp = temp.substring(0, end) + newText + originalText.substring(end);
		editor.getDocumentProvider().getDocument(editor.getEditorInput())
				.set(temp);
	}
	
	/**
	 * Changes table cell data
	 * @param item - table row item
	 * @param row - row index
	 * @param col - column index
	 * @param text - cell text
	 */
	public void changeRowData(TableItem item, int row, int col, Text text) {
		item.setText(col, text.getText());
		for (int i = 0; i < text.getText().length(); i++) {
			String symbols = pm.getFontString(text.getText());
			table.getItem(row)
					.setText(i + 1, String.valueOf(symbols.charAt(i)));
		}
	}
	
	/**
	 * Checks current file size 
	 */
	public void checkFileSize() {
		String file = editor.getDocumentProvider()
				.getDocument(editor.getEditorInput()).get();
		text.setText(file);
		
		try {
			byte[] fileByte = file.getBytes("UTF-8");
			fileSizeLabel.setText("Filesize: " + fileByte.length + " bytes");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Loads special fonts
	 */
    public void getFonts(){
    	xDigitsClockIs = MultiPageEditor.class.getResourceAsStream("/xDigitsClock.ttf");
        xDigitsSansIs = MultiPageEditor.class.getResourceAsStream("/xDigitsSans.ttf");
    }
    
    public void showFontTable(int testint){
		try {
			showFontTable2(testint);
		} catch (FontFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

}

class LengthValidator implements IInputValidator {
	/**
	 * Validates the String. Returns null for no error, or an error message
	 * 
	 * @param newText
	 *            the String to validate
	 * @return String
	 */
	public String isValid(String newText) {
		int len = newText.length();

		// Determine if input is too short or too long
		if (len < 16)
			return "Too short";
		if (len > 16)
			return "Too long";

		// Input must be OK
		return null;
	}
}
