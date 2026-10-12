param([string]$Docx, [string]$Pdf)
$word = New-Object -ComObject Word.Application
$word.Visible = $false
$word.DisplayAlerts = 0
try {
    $doc = $word.Documents.Open($Docx, $false, $false)
    foreach ($id in -20, -21, -22) {
        $style = $doc.Styles.Item($id)
        $style.ParagraphFormat.ReadingOrder = 0
        $style.ParagraphFormat.Alignment = 3
        $style.Font.Name = "Times New Roman"
        $style.Font.NameBi = "Times New Roman"
        $style.Font.SizeBi = 13
        $style.Font.Size = 13
    }
    foreach ($toc in $doc.TablesOfContents) { $toc.Update() }
    $doc.Fields.Update() | Out-Null
    foreach ($toc in $doc.TablesOfContents) {
        $toc.Update()
        $toc.Range.ParagraphFormat.ReadingOrder = 0
        $toc.Range.ParagraphFormat.Alignment = 3
    }
    "toc1 reading order: " + $doc.Styles.Item(-20).ParagraphFormat.ReadingOrder
    $doc.Save()
    $doc.ExportAsFixedFormat($Pdf, 17)
    $doc.ExportAsFixedFormat(($Docx -replace '\.docx$', '.pdf'), 17)
    "pages: " + $doc.ComputeStatistics(2)
    $doc.Close($false)
} finally {
    $word.Quit()
    [System.Runtime.InteropServices.Marshal]::ReleaseComObject($word) | Out-Null
}
