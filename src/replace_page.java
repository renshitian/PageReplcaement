import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.LinkedList;

public class replace_page {

	private static int memSize;
	NumberFormat formatter = new DecimalFormat("#0.00");
	public static void main(String[] args) {
	
		if(args.length!=3){
			System.out.println("Please type in replace_page [memory size] [replacement strategy 0:FIFO 1:2nd_Chance 2:LRU] [file name]");
			System.exit(0);
		}
		
		replace_page rp = new replace_page();
		memSize = Integer.parseInt(args[0]);
		int type = Integer.parseInt(args[1]);
		
		String fileName = args[2];

		Integer[] input = rp.readFile(fileName);
		
		
		if(type==0)
		rp.replacePage_FIFO(input);
		else if(type==1)
		rp.replacePage_2nd(input);
		else if(type==2)
		rp.replacePage_LRU(input);
		else
			System.out.println("Not identified type of strategy");
	}

	public void replacePage_LRU(Integer[] input) {
		int round = 0;
		int pageFault = 0;
		int length = 0;
		String fileStr="";
		LinkedList<Page2nd> memForPage = new LinkedList<Page2nd>();
		Integer[] memory = new Integer[memSize];

		for (int i = 0; i < input.length; i++) {
			round++;
			int requiredPage = input[i];

			if (checkPageInMem_2nd(requiredPage, memForPage)) {
				Page2nd tempPage = null;
				for(Page2nd p : memForPage){
					if(p.pageNo==requiredPage){
						tempPage = p;
						memForPage.remove(p);
						break;
					}
				}
				memForPage.addFirst(tempPage);
				fileStr += showMemory(memory, length);
				continue;
			} else {
				pageFault++;
				if (memForPage.size() < memSize) {
					int temp = memForPage.size();
					memory[temp] = requiredPage;
					length++;
					Page2nd newPage = new Page2nd(1, requiredPage, temp);
					memForPage.addFirst(newPage);
					fileStr += showMemory(memory, length);
					continue;
				}
				Page2nd newPage = new Page2nd(1, requiredPage, -1);
				Page2nd oldPage = memForPage.getLast();
				memForPage.removeLast();
				int memNo = oldPage.memNo;
				newPage.memNo = memNo;
				memory[memNo] = newPage.pageNo;
				memForPage.addFirst(newPage);
				fileStr += showMemory(memory, length);
			}
		}
		double percent = (double) pageFault / (double) round;
		fileStr += ("Precentage of page faults: "+formatter.format(percent));
		writeFile(fileStr,"LRU");
		System.out.println("Precentage of page fault: " + formatter.format(percent));
		
	}

	public void replacePage_2nd(Integer[] input) {
		int round = 0;
		int pageFault = 0;
		int length = 0;
		String fileStr="";
		LinkedList<Page2nd> memForPage = new LinkedList<Page2nd>();
		Integer[] memory = new Integer[memSize];
		for (int i = 0; i < input.length; i++) {
			round++;
			int requiredPage = input[i];
			if (checkPageInMem_2nd(requiredPage, memForPage)) {
				resetRBit(requiredPage, memForPage);
				fileStr += showMemory(memory, length);
				continue;
			} else {
				pageFault++;
				if (memForPage.size() < memSize) {
					int temp = memForPage.size();
					memory[temp] = requiredPage;
					length++;
					Page2nd newPage = new Page2nd(1, requiredPage, temp);
					memForPage.add(newPage);
					fileStr += showMemory(memory, length);
					continue;
				}
				while (!checkRbit(memForPage)) {
				}
				Page2nd newPage = new Page2nd(1, requiredPage, -1);
				Page2nd oldPage = memForPage.poll();
				int memNo = oldPage.memNo;
				newPage.memNo = memNo;
				memory[memNo] = newPage.pageNo;
				memForPage.add(newPage);
				fileStr += showMemory(memory, length);
			}
		}
		double percent = (double) pageFault / (double) round;
		fileStr += ("Precentage of page faults: "+formatter.format(percent));
		writeFile(fileStr,"2ndChance");
		System.out.println("Precentage of page fault: " + formatter.format(percent));
	}

	private void resetRBit(int requiredPage, LinkedList<Page2nd> memForPage) {
		for (Page2nd page : memForPage) {
			if (page.pageNo == requiredPage) {
				page.r = 1;
			}
		}
	}

	private boolean checkRbit(LinkedList<Page2nd> memForPage) {
		if (memForPage.isEmpty())
			return true;
		if (memForPage.peek().r == 0) {
			return true;
		} else if (memForPage.peek().r == 1) {
			Page2nd newPage = memForPage.poll();
			newPage.r--;
			memForPage.add(newPage);
			return false;
		}
		return false;
	}

	private boolean checkPageInMem_2nd(int node, LinkedList<Page2nd> memForPage) {
		if (memForPage.isEmpty())
			return false;
		for (Page2nd page : memForPage) {
			if (page.pageNo == node) {
				return true;
			}
		}
		return false;
	}

	public void replacePage_FIFO(Integer[] input) {
		int swapInIndex = 1;
		int round = 0;
		int pageFault = 0;
		int length = 0;
		String fileStr="";
		Integer[] memoryForPage = new Integer[memSize];
		for (int i = 0; i < input.length; i++) {
			round++;
			int requiredPage = input[i];

			if (checkPageInMem_FIFO(requiredPage, memoryForPage, length)) {
				fileStr += showMemory(memoryForPage, length);
				continue;
			} else {
				pageFault++;
				memoryForPage[swapInIndex - 1] = requiredPage;
				if (!(length == memSize)) {
					length++;
				}
				swapInIndex++;
				if (swapInIndex == memSize + 1) {
					swapInIndex = 1;
				}
			}
			fileStr += showMemory(memoryForPage, length);
		}
		double percent = (double) pageFault / (double) round;
		fileStr += ("Precentage of page faults: "+formatter.format(percent));
		writeFile(fileStr,"FIFO");
		System.out.println("Precentage of page fault:" + formatter.format(percent));
	}

	private String showMemory(Integer[] memoryForPage, int length) {
		String result = "";
		for (int d = 0; d < length; d++) {
			System.out.print(memoryForPage[d] + " ");
			result += (memoryForPage[d]+" ");
		}
		result += ("\r\n");
		System.out.println("");
		return result;
	}

	private boolean checkPageInMem_FIFO(int requiredPage,
			Integer[] memoryForPage, int length) {
		for (int d = 0; d < length; d++) {
			if (requiredPage == memoryForPage[d])
				return true;
		}
		return false;
	}
	
	public void writeFile(String fileWrite,String no){
		File fl = new File("output"+"-"+no+".txt");
		FileWriter fw;
		try {
			fw = new FileWriter(fl);
			fw.write(fileWrite);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	public Integer[] readFile(String fileName) {
		String dataStr = "";
		try {
			FileReader fr = new FileReader(fileName);
			BufferedReader br = new BufferedReader(fr);

			String da;
			while ((da = br.readLine()) != null) {
				dataStr += da;
				dataStr += " ";
			}
			br.close();
			fr.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		String[] data = dataStr.trim().split(" ");
		Integer[] result = new Integer[data.length];
		for (int i = 0; i < data.length; i++) {
			result[i] = Integer.parseInt(data[i]);
		}
		return result;

	}

	class Page2nd {
		int r;
		int pageNo;
		int memNo;

		public Page2nd(int r, int pageNo, int memNo) {
			this.r = r;
			this.pageNo = pageNo;
			this.memNo = memNo;
		}
	}
}
