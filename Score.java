
public class Score implements Comparable<Score>
{
	public String pageID;
	public double body;
	public double title;
	public double overall;

	public Score(String id, double _body, double _title) {
		this.pageID = id;
		this.body = _body;
		this.title = _title;
		this.overall = this.body + 2.0 * this.title;
	}

	public Score(double _body, double _title) {
		this.body = _body;
		this.title = _title;
		this.overall = this.body + 2.0 * this.title;
	}

	@Override
	public int compareTo(Score o) {
		if(this.overall > o.overall)
			return -1;
		else if(this.overall == o.overall)
			return 0;
		else
			return 1;
	}

}